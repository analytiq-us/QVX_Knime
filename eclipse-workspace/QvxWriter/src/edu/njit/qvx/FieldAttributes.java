//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.02.20 at 07:51:10 PM EST 
//


package edu.njit.qvx;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FieldAttributes complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FieldAttributes"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;all&gt;
 *         &lt;element name="Type" type="{}FieldAttrType"/&gt;
 *         &lt;element name="nDec" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="UseThou" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="Fmt" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Dec" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Thou" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/all&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FieldAttributes", propOrder = {

})
public class FieldAttributes {

    @XmlElement(name = "Type", required = true)
    @XmlSchemaType(name = "string")
    protected FieldAttrType type;
    protected BigInteger nDec;
    @XmlElement(name = "UseThou")
    protected BigInteger useThou;
    @XmlElement(name = "Fmt")
    protected String fmt;
    @XmlElement(name = "Dec")
    protected String dec;
    @XmlElement(name = "Thou")
    protected String thou;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link FieldAttrType }
     *     
     */
    public FieldAttrType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link FieldAttrType }
     *     
     */
    public void setType(FieldAttrType value) {
        this.type = value;
    }

    /**
     * Gets the value of the nDec property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNDec() {
        return nDec;
    }

    /**
     * Sets the value of the nDec property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setNDec(BigInteger value) {
        this.nDec = value;
    }

    /**
     * Gets the value of the useThou property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getUseThou() {
        return useThou;
    }

    /**
     * Sets the value of the useThou property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setUseThou(BigInteger value) {
        this.useThou = value;
    }

    /**
     * Gets the value of the fmt property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFmt() {
        return fmt;
    }

    /**
     * Sets the value of the fmt property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFmt(String value) {
        this.fmt = value;
    }

    /**
     * Gets the value of the dec property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDec() {
        return dec;
    }

    /**
     * Sets the value of the dec property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDec(String value) {
        this.dec = value;
    }

    /**
     * Gets the value of the thou property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getThou() {
        return thou;
    }

    /**
     * Sets the value of the thou property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setThou(String value) {
        this.thou = value;
    }

}
