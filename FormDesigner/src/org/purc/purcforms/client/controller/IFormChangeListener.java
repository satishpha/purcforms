package org.purc.purcforms.client.controller;


/**
 * Interface for listening to form changes during design.
 * 
 * @author daniel
 *
 */
public interface IFormChangeListener {

	/**
	 * Called when a form item (form,page,question or question option) is changed.
	 * 
	 * @param formItem the item which has been changed.
	 */
	public void onFormItemChanged(Object formItem);
	
	/**
	 * Called when it is time to deleetd the kids of a form item (QuestionDef,PageDef).
	 * 
	 * @param formItem the form item whose kids are to be deleted.
	 */
	public void onDeleteChildren(Object formItem);
}