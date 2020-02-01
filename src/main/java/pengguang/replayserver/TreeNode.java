package pengguang.replayserver;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TreeNode<T> implements Iterable<TreeNode<T>> {

	public T data;
	public TreeNode<T> parent;
	public List<TreeNode<T>> children;
	public int count;

	public boolean isRoot() {
		return parent == null;
	}

	public boolean isLeaf() {
		return children.size() == 0;
	}

	public TreeNode(T data) {
		this.data = data;
		this.children = new LinkedList<TreeNode<T>>();
		count = 1;
	}

	public TreeNode<T> addChild(T child) {
		TreeNode<T> childNode = new TreeNode<T>(child);
		childNode.parent = this;
		this.children.add(childNode);
		this.registerChildForSearch(childNode);
		return childNode;
	}

	public TreeNode<T> remove() {
        for (TreeNode<T> node: this) {
            this.unregisterChildForSearch(node);
        }

	    if (parent!= null) {
	        parent.children.remove(this);
        }

	    TreeNode<T> parentNode = parent;
	    parent = null;
        return parentNode;
    }

	public int getLevel() {
		if (this.isRoot())
			return 0;
		else
			return parent.getLevel() + 1;
	}

	public TreeNode<T> getSibling() {
	    if (parent == null) {
	        return null;
        }

        int i;
        for (i=0; i<parent.children.size(); i++) {
            if (parent.children.get(i) == this) {
                break;
            }
        }

        if (i<parent.children.size()-1) {
            return parent.children.get(i+1);
        } else {
            return null;
        }
    }

    public TreeNode<T> getNextSibling() {
        return getSibling();
    }

    public TreeNode<T> getPrevSibling() {
	    if (parent == null) {
	        return null;
        }

        int i;
        for (i=0; i<parent.children.size(); i++) {
            if (parent.children.get(i) == this) {
                break;
            }
        }

        if (i > 0) {
            return parent.children.get(i-1);
        } else {
            return null;
        }
    }

	private void registerChildForSearch(TreeNode<T> node) {
		count++;
		if (parent != null)
			parent.registerChildForSearch(node);
	}

	private void unregisterChildForSearch(TreeNode<T> node) {
		count--;
		if (parent != null)
			parent.unregisterChildForSearch(node);
	}

	@Override
	public String toString() {
		return data != null ? data.toString() : "[data null]";
	}

	@Override
	public Iterator<TreeNode<T>> iterator() {
		TreeNodeIter<T> iter = new TreeNodeIter<T>(this);
		return iter;
	}

}
