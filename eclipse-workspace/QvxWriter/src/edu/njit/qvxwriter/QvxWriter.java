package edu.njit.qvxwriter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.node.BufferedDataTable;

import edu.njit.qvx.QvxFieldExtent;
import edu.njit.qvx.QvxFieldType;
import edu.njit.qvx.QvxNullRepresentation;
import edu.njit.qvx.QvxTableHeader;
import edu.njit.qvx.QvxTableHeader.Fields.QvxFieldHeader;
import edu.njit.util.Util;

public class QvxWriter {
	
	private BufferedDataTable table;
	private String[] fieldNames;
	private String[][] data;
	private String outFileName;

	private QvxTableHeader tableHeader;
	private int bufferIndex = 0;
	private FileOutputStream outputStream;
	
	private static final int BUFFER_SIZE = (int)Math.pow(2, 20); //1 MB of memory
	private static final byte RECORD_SEPARATOR = 0x1E;
	private static final byte FILE_SEPARATOR = 0x1C;
	private static final byte NUL = 0x00;
	
	//public void writeQvxFile(BufferedDataTable table, String qvxFileName, QvxTableHeader _tableHeader) {
	
	public void writeQvxFile(BufferedDataTable table, String outFileName) {
		this.table = table;
		this.fieldNames = table.getSpec().getColumnNames();
		this.outFileName = outFileName;
		this.data = dataTableToArray(table);
		
		/*
		writeTableHeader();
		writeBody();
		dataTableToArray(table);
		*/
	}
	
	private void writeTableHeader() {
				
		QvxTableHeader.Fields fields = new QvxTableHeader.Fields();
		for(int i = 0; i < fieldNames.length; i++) {
			/* Create a QvxFieldHeader for each field */

			String[] column = getDataColumn(i);
			QvxTableHeader.Fields.QvxFieldHeader qvxFieldHeader = new QvxTableHeader.Fields.QvxFieldHeader();
			
			qvxFieldHeader.setFieldName(fieldNames[i]);
			setFieldTypeAndByteWidth(qvxFieldHeader, column);
			qvxFieldHeader.setExtent(determineExtent(qvxFieldHeader));
			qvxFieldHeader.setNullRepresentation(QvxNullRepresentation.QVX_NULL_NEVER);
			if (qvxFieldHeader.isBigEndian() == null) { //Uses little-endian by default
				qvxFieldHeader.setBigEndian(false);
			}			
			fields.getQvxFieldHeader().add(qvxFieldHeader);
		}
		tableHeader.setFields(fields);
		
		try {
			//Marshal qvxTableHeader into a FileOutputStream, then write a null byte
			JAXBContext jaxbContext;
			jaxbContext = JAXBContext.newInstance(QvxTableHeader.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			outputStream = new FileOutputStream(outFileName);
			jaxbMarshaller.marshal(tableHeader, outputStream);
			outputStream.write(NUL); //Zero-byte separator between xml and qvx body
		}
		catch(IOException | JAXBException e) {
			e.printStackTrace();
		}
	}
	
	private void writeBody() {
		
		/* Write the "data" values to the body of the FileOutputStream
		 */
		
		//Read all rows except for the first row
		byte[] buffer = new byte[BUFFER_SIZE];
		for(int i = 1; i < data.length; i++) {
			for(int j = 0; j < data[i].length; j++) {
				//"fieldHeader" is the fieldHeader for the column that is being accessed
				QvxTableHeader.Fields.QvxFieldHeader fieldHeader = tableHeader.getFields().getQvxFieldHeader().get(j);
				byte[] byteValue = convertToByteValue(fieldHeader, data[i][j]);
				
				//Write "byteValue" to buffer
				int prevBufferIndex = bufferIndex; //Necessary for dealing with buffer overflow
				bufferIndex = insertInto(buffer, byteValue, bufferIndex);
				if (bufferIndex == -1) { //If there would have been buffer overflow
					// Write the entire buffer to the outputStream (excluding the value that could not be entered),
					// then insert this value at beginning of buffer
					try {
						outputStream.write(Arrays.copyOfRange(buffer, 0, prevBufferIndex));
					}catch(IOException e) {
						e.printStackTrace();
					}				
					bufferIndex = insertInto(buffer, byteValue, 0);
				}
			}
		}
		try { //Write any characters that are still in the buffer to the outputStream
			outputStream.write(Arrays.copyOfRange(buffer, 0, bufferIndex));
			outputStream.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}

	//Helper methods --------------------------------------------------
	private byte[] convertToByteValue(QvxFieldHeader fieldHeader, String s) {
					
		int byteWidth = fieldHeader.getByteWidth().intValue();
		ByteBuffer byteBuffer = null;
		switch (fieldHeader.getType()) {
			case QVX_SIGNED_INTEGER:
			case QVX_UNSIGNED_INTEGER:
				byteBuffer = ByteBuffer.allocate(byteWidth);
				byteBuffer.order(fieldHeader.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
				if (byteWidth == 4) {
					byteBuffer.putInt(Integer.parseInt(s));
				}else if(byteWidth == 8) {
					byteBuffer.putLong(Long.parseLong(s));
				}
				return byteBuffer.array();
			case QVX_IEEE_REAL:
				byteBuffer = ByteBuffer.allocate(byteWidth);
				byteBuffer.order(fieldHeader.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
				if (byteWidth == 4) {
					byteBuffer.putFloat(Float.parseFloat(s));
				}else if(byteWidth == 8) {
					byteBuffer.putDouble(Double.parseDouble(s));
				}
				return byteBuffer.array();
			case QVX_TEXT:
				return stringToByteArray_zeroTerminated(fieldHeader, s);
			default:
				return null;
		}
	}
	
	private String[][] dataTableToArray(BufferedDataTable table){
		
		String[][] arr = new String[(int)table.size()][table.getSpec().getNumColumns()];
		CloseableRowIterator iterator = table.iterator();
		int rowIndex = 0;
        while (iterator.hasNext()) {
        	DataRow row = iterator.next();
        	for(int columnIndex = 0; columnIndex < row.getNumCells(); columnIndex++) {
        		//String type = spec.getColumnSpec(columnIndex).getType().getName();
        		DataCell cell = row.getCell(columnIndex);
        		arr[rowIndex][columnIndex] = cell.toString();
        		
        		/*//TODO: Save everything as strings for now, during this step
        		if (type.equals("Number (integer)")) {
        			System.out.println("Type is integer");
        		}else if (type.equals("Number (double)")) {
        			System.out.println("Type is double");
        		}else if (type.equals("String")) {
        			System.out.println("Type is string");
        		}
        		String s = cell.toString();*/
        	}
        	System.out.println();
        	rowIndex++;
        }
		return arr;
	}
	
	/*
	public static QvxTableHeader defaultTableHeader(){
		
		QvxTableHeader tableHeader = new QvxTableHeader();
		tableHeader.setMajorVersion(BigInteger.valueOf(1));
		tableHeader.setMinorVersion(BigInteger.valueOf(0));
		
		//Set the date //TODO: Data format for now is not completely correct; fix
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		try {
			tableHeader.setCreateUtcTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar));
		}
		catch(DatatypeConfigurationException e) { e.printStackTrace(); }
		
		return tableHeader;
	}*/
	
	private QvxFieldExtent determineExtent(QvxFieldHeader fieldHeader) {
		
		if (fieldHeader.getType() == QvxFieldType.QVX_TEXT) {
			return QvxFieldExtent.QVX_ZERO_TERMINATED;
		}else {
			return QvxFieldExtent.QVX_FIX;
		}
	}
	
	private static String[] getDataColumn(int j) {
		/*
		String[] column = new String[data.length - 1];
		for(int i = 1; i < data.length; i++) {
			column[i-1] = data[i][j];
		}
		return column;*/
		return null;
	}
	
	private static int insertInto(byte[] target, byte[] values, int offset) {
		
		/* Copy all of the elements of "values" into "target", starting at target[startIndex]. Return
		 * the index of "target" where the next inserted value would go. If there would be buffer overflow, do not do any insertions and
		 * return -1.
		 */
		
		if (offset + values.length > target.length) {
			return -1;
		}
		int i;
		for(i = offset; i < offset + values.length; i++) {
			target[i] = values[i - offset];
		}
		return i;
	}
	
	private void setFieldTypeAndByteWidth(QvxFieldHeader fieldHeader, String[] values) {
		
		// Assume the type is QVX_UNSIGNED_INTEGER, and byte width is 4; overrule these assumptions if necessary
		fieldHeader.setType(QvxFieldType.QVX_UNSIGNED_INTEGER);
		fieldHeader.setByteWidth(BigInteger.valueOf(4));
		boolean signFound = false;
		
		for(String value: values) {
			//If the value has a decimal point or is text, change the field type. For text, set the byteWidth to 0.
			boolean decimalFound = false;
			for(int i = 0; i < value.length(); i++) {
				char c = value.charAt(i);
				if (c < '0' || c > '9') {
					if (c == '.' && !decimalFound) {
						decimalFound = true;
						fieldHeader.setType(QvxFieldType.QVX_IEEE_REAL);
					}else if (c == ',') {
						//TODO: Deal with thousand separator
					}else if (c == '-' && i == 0) {
						signFound = true;
					}else{
						fieldHeader.setType(QvxFieldType.QVX_TEXT);
						fieldHeader.setByteWidth(BigInteger.valueOf(0));
						return;
					}
				}
			}
			
			//For integer value, if conversion to int is not possible, set byteWidth to 8
			QvxFieldType fieldHeaderType = fieldHeader.getType();
			if (fieldHeaderType == QvxFieldType.QVX_UNSIGNED_INTEGER || fieldHeaderType == QvxFieldType.QVX_SIGNED_INTEGER) {
				if (signFound) {
					fieldHeader.setType(QvxFieldType.QVX_SIGNED_INTEGER);
				}				
				try {
					Integer.parseInt(value);					
				}catch (NumberFormatException e1) {
					try {
						Long.parseLong(value);
						fieldHeader.setByteWidth(BigInteger.valueOf(8));
					}catch (NumberFormatException e2) {
						e2.printStackTrace();
					}
				}								
			}
			
			//Use byteWidth of 8 for all floating point numbers
			if (fieldHeader.getType() == QvxFieldType.QVX_IEEE_REAL) {
				fieldHeader.setByteWidth(BigInteger.valueOf(8));
			}
		}
	}
	
	private byte[] stringToByteArray_zeroTerminated(QvxFieldHeader fieldHeader, String s) {
		
		//TODO: QVX file has a bunch of different null values; why does this happen?
		Integer codePage = null;
		if(fieldHeader.getCodePage() != null) {
			codePage = fieldHeader.getCodePage().intValue();
		}
		
		if (codePage == null) {
			byte[] bytes = new byte[s.length() + 1];
			for(int i = 0; i < s.length(); i++) {
				bytes[i] = (byte)s.charAt(i);
			}
			bytes[bytes.length-1] = (byte)0; //Zero-terminated byte
			return bytes;
		}else if (codePage == 1020 || codePage == 1021) { //UTF-16
			//TODO
			throw new IllegalStateException("Code not yet implemented");
		}
		throw new IllegalStateException("Unrecognized code page");
	}
	
	/*public static void main(String[] args) {
		
		 //Test #1
		 QvxTableHeader tableHeader = defaultTableHeader();
		 writeQvxFromCsv("products.csv", "products.qvx", tableHeader);
	}*/
}
