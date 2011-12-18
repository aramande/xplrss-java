import java.io.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import javax.swing.*;
import javax.swing.tree.*;

/**
 * Most of the code in this file was inspired/copied from 
 * http://www.java2s.com/Code/Java/Swing-JFC/DnDdraganddropJTreecode.htm
 * and all rights are reserved by the author.
 */
public class DragAndDrop implements DragSourceListener, DragGestureListener, DropTargetListener{
    JTree sourceTree;
    JTree targetTree;
    DragSource source;
    DropTarget target;
    Feed userObject;
    TreePath actualPath;

    DragGestureRecognizer recognizer;
    TransferableTreeNode transferable;
    DefaultMutableTreeNode oldNode;

    public DragAndDrop(JTree sourceTree, JTree targetTree, int actions){
        this.sourceTree = sourceTree;
        this.targetTree = targetTree;
        this.source = new DragSource();
        this.target = new DropTarget(targetTree, this);
        this.recognizer = source.createDefaultDragGestureRecognizer(sourceTree, actions, this);
    }

    private TreeNode getNodeForEvent(DropTargetEvent e) {
        Point p = null;
        if(e instanceof DropTargetDropEvent){
            p = ((DropTargetDropEvent)e).getLocation();
        }
        if(e instanceof DropTargetDragEvent){
            p = ((DropTargetDragEvent)e).getLocation();
        }
        DropTargetContext targetContext = e.getDropTargetContext();
        JTree tree = (JTree) targetContext.getComponent();
        TreePath path = tree.getClosestPathForLocation(p.x, p.y);
        return (TreeNode) path.getLastPathComponent();
    }

    //DragSourceListener
    @Override
        public void dragDropEnd(DragSourceDropEvent e){
            System.out.println("dragDropEnd");
            
            if(e.getDropSuccess() && (e.getDropAction() == DnDConstants.ACTION_MOVE)){
                ((DefaultTreeModel)sourceTree.getModel()).removeNodeFromParent(oldNode);
            }
            ((Feed)FeedTree.init().getRoot().getUserObject()).updateTreeNode();

            SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    FeedTree.init().saveToFile(null);
                }
            });
        }
    @Override
        public void dragOver(DragSourceDragEvent e){
            System.out.println("dragOver");
        }
    @Override
        public void dragEnter(DragSourceDragEvent e){
            System.out.println("dragEnter");
        }
    @Override
        public void dropActionChanged(DragSourceDragEvent e){
            System.out.println("dropActionChanged");
        }
    @Override
        public void dragExit(DragSourceEvent e){
            System.out.println("dragExit");
        }

    
    //DropSourceListener
    @Override
        public void drop(DropTargetDropEvent e){
            System.out.println("drop");
            Point pt = e.getLocation();
            DropTargetContext targetContext = e.getDropTargetContext();
            JTree tree = (JTree)targetContext.getComponent();
            TreePath parentpath = tree.getClosestPathForLocation(pt.x, pt.y);
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode)parentpath.getLastPathComponent(); //(DefaultMutableTreeNode)getNodeForEvent(e);
            if(!parent.getAllowsChildren()){
                e.rejectDrop();
                return;
            }

            try {
                Transferable transferable = e.getTransferable();
                DataFlavor[] flavors = transferable.getTransferDataFlavors();
                TreePath path;
                DefaultMutableTreeNode node;
                DefaultTreeModel model;
                for (int i = 0; i < flavors.length; i++) {
                    if (transferable.isDataFlavorSupported(flavors[i])) {
                        path = (TreePath)transferable.getTransferData(flavors[i]);
                        node = (DefaultMutableTreeNode)path.getLastPathComponent();

                        e.acceptDrop(e.getDropAction());

                        model = (DefaultTreeModel)tree.getModel();

                        model.insertNodeInto(node, parent, 0);

            //          ((CompoundFeed)parent.getUserObject()).update();
            //          ((CompoundFeed)((DefaultMutableTreeNode)oldNode.getParent()).getUserObject()).update();
                        e.dropComplete(true);
                        //((Feed)((DefaultMutableTreeNode)model.getRoot()).getUserObject()).init();
                        return;
                    }
                }
                e.rejectDrop();
            } catch (Exception ex) {
                ex.printStackTrace();
                e.rejectDrop();
            }
        }

    @Override
        public void dragOver(DropTargetDragEvent e){
            System.out.println("dragOver");
            TreeNode node = getNodeForEvent(e);
            if(!node.getAllowsChildren()){
                e.rejectDrag();
            }
            else{
                e.acceptDrag(e.getDropAction());
            }
        }
    @Override
        public void dragEnter(DropTargetDragEvent e){
            System.out.println("dragEnter");
        }
    @Override
        public void dropActionChanged(DropTargetDragEvent e){
            System.out.println("dropActionChanged");
        }
    @Override
        public void dragExit(DropTargetEvent e){
            System.out.println("dragExit");
            DefaultTreeModel model = (DefaultTreeModel)sourceTree.getModel();
            //oldNode.setUserObject(userObject);
            model.nodeChanged(oldNode);
        }

    //DragGestureListener 
    @Override
        public void dragGestureRecognized(DragGestureEvent e){
            System.out.println("dragGestureRecognized");
            TreePath path = sourceTree.getSelectionPath();
            if ((path == null) || (path.getPathCount() <= 1)) {
                // We can't move the root node or an empty selection
                return;
            }

            //((Feed)((DefaultMutableTreeNode)path.getPathComponent(0)).getUserObject()).unInit();

            oldNode = (DefaultMutableTreeNode) path.getLastPathComponent();

            transferable = new TransferableTreeNode(path);
            //source.startDrag(e, DragSource.DefaultMoveNoDrop, transferable, this);

            // If you support dropping the node anywhere, you should probably
            // start with a valid move cursor:
            source.startDrag(e, DragSource.DefaultMoveDrop, transferable, this);
        }
}

class TransferableTreeNode implements Transferable{
    public static DataFlavor TREE_PATH_FLAVOR = new DataFlavor(TreePath.class, "Tree Path");

    DataFlavor flavors[] = { TREE_PATH_FLAVOR };

    TreePath path;

    public TransferableTreeNode(TreePath tp){
        path = tp;
    }

    public synchronized DataFlavor[] getTransferDataFlavors(){
        return flavors;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor){
        return (flavor.getRepresentationClass() == TreePath.class);
    }

    public synchronized Object getTransferData(DataFlavor flavor)
        throws UnsupportedFlavorException, IOException{
        if(isDataFlavorSupported(flavor)){
            return (Object) path;
        }
        else{
            throw new UnsupportedFlavorException(flavor);
        }
    }
}

