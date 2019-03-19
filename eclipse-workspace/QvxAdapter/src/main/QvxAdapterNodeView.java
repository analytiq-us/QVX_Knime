package edu.njit.knime.adapter.nodes.qvx;

import org.knime.core.node.NodeView;

/**
 * <code>NodeView</code> for the "QvxAdapter" Node.
 * Reading and writing different qvx file formats and figuring out how to convert them into table format
 *
 * @author Simple Qvx Adapter to Read and Write 
 */
public class QvxAdapterNodeView extends NodeView<QvxAdapterNodeModel> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link QvxAdapterNodeModel})
     */
    protected QvxAdapterNodeView(final QvxAdapterNodeModel nodeModel) {
        super(nodeModel);

        // TODO instantiate the components of the view here.

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {

        // TODO retrieve the new model from your nodemodel and 
        // update the view.
        QvxAdapterNodeModel nodeModel = 
            (QvxAdapterNodeModel)getNodeModel();
        assert nodeModel != null;
        
        // be aware of a possibly not executed nodeModel! The data you retrieve
        // from your nodemodel could be null, emtpy, or invalid in any kind.
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
    
        // TODO things to do when closing the view
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {

        // TODO things to do when opening the view
    }

}