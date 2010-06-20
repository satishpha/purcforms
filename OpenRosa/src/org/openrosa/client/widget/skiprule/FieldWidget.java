package org.openrosa.client.widget.skiprule;

import java.util.List;

import org.openrosa.client.model.FormDef;
import org.openrosa.client.model.GroupDef;
import org.openrosa.client.model.IFormElement;
import org.openrosa.client.model.QuestionDef;
import org.openrosa.client.util.FormDesignerUtil;
import org.purc.purcforms.client.controller.ItemSelectionListener;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;


/**
 * This widget is used to let one select a field or question for a skip or validation
 * rule condition. For validation rules, this can be used for the condition value. eg
 * Weight less than Height. For skip rules, this can be used for both the condition
 * question and value.
 * 
 * @author daniel
 *
 */
public class FieldWidget extends Composite{

	/** The text to display when no value is specified for a condition. */
	private static final String EMPTY_VALUE = "_____";
	
	/** The form to which the question, represented by this widget, belongs. */
	private FormDef formDef;
	
	/** The main widget. */
	private HorizontalPanel horizontalPanel;
	
	/** The widget to do auto suggest for form questions as the user types. */
	private SuggestBox sgstField = new SuggestBox();
	
	/** The text field where to type the question name. */
	private TextBox txtField = new TextBox();
	
	/** The widget to display the selected question text when not in selection mode. */
	private Hyperlink fieldHyperlink;
	
	/** The listener for item selection events. */
	private ItemSelectionListener itemSelectionListener;
	
	/** A flag determining if the current field selection is for a single select dynamic.
	 * type of question.
	 */
	private boolean forDynamicOptions = false;
	
	/** The single select dynamic question. */
	private QuestionDef dynamicQuestionDef;

	
	public FieldWidget(ItemSelectionListener itemSelectionListener){
		this.itemSelectionListener = itemSelectionListener;
		setupWidgets();
	}
	
	/**
	 * Sets the form to which the referenced question belongs.
	 * 
	 * @param formDef the form definition object.
	 */
	public void setFormDef(FormDef formDef){
		this.formDef = formDef;
		setupPopup();
	}
	
	private void setupWidgets(){
		fieldHyperlink = new Hyperlink("",""); //Field 1
		
		horizontalPanel = new HorizontalPanel();
		horizontalPanel.add(fieldHyperlink);

		fieldHyperlink.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event){
				itemSelectionListener.onStartItemSelection(this);
				horizontalPanel.remove(fieldHyperlink);
				horizontalPanel.add(sgstField);
				sgstField.setText(fieldHyperlink.getText());
				sgstField.setFocus(true);
				txtField.selectAll();
			}
		});
		
		/*txtField.addFocusListener(new FocusListenerAdapter(){
			public void onLostFocus(Widget sender){
				stopSelection();
			}
		});
		
		/*sgstField.addFocusListener(new FocusListenerAdapter(){
			public void onLostFocus(Widget sender){
				stopSelection();
			}
		});*/
		
		sgstField.addSelectionHandler(new SelectionHandler(){
			public void onSelection(SelectionEvent event){
				stopSelection();
			}
		});
		
		initWidget(horizontalPanel);
	}
	
	public void stopSelection(){
		if(horizontalPanel.getWidgetIndex(fieldHyperlink) != -1)
			return;
		
		String val = sgstField.getText();
		if(val.trim().length() == 0)
			val = EMPTY_VALUE;
		fieldHyperlink.setText(val);
		horizontalPanel.remove(sgstField);
		horizontalPanel.add(fieldHyperlink);
		IFormElement qtn = formDef.getQuestionWithText(txtField.getText());
		if(qtn != null)
			itemSelectionListener.onItemSelected(this,qtn);
	}
	
	private void setupPopup(){
		MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();

		//for(int i=0; i<formDef.getPageCount(); i++)
		//	FormDesignerUtil.loadQuestions(formDef.getPageAt(i).getQuestions(),dynamicQuestionDef,oracle,forDynamicOptions);
		
		FormDesignerUtil.loadQuestions(formDef.getChildren(),dynamicQuestionDef,oracle,forDynamicOptions);

		txtField = new TextBox(); //TODO New and hence could be buggy
		sgstField = new SuggestBox(oracle,txtField);
		selectFirstQuestion();
		
		sgstField.addSelectionHandler(new SelectionHandler(){
			public void onSelection(SelectionEvent event){
					stopSelection();
			}
		});
		
		/*sgstField.addFocusListener(new FocusListenerAdapter(){
			public void onLostFocus(Widget sender){
				stopSelection();
			}
		});*/
	}
	
	public void selectQuestion(IFormElement questionDef){
		fieldHyperlink.setText(questionDef.getText());
		itemSelectionListener.onItemSelected(this, questionDef);
	}
	
	private void selectFirstQuestion(){
		/*for(int i=0; i<formDef.getPageCount(); i++){
			if(selectFirstQuestion(formDef.getPageAt(i).getQuestions()))
				return;
		}*/
		selectFirstQuestion(formDef.getChildren());
	}
	
	private boolean selectFirstQuestion(List<IFormElement> questions){
		for(int i=0; i<questions.size(); i++){
			IFormElement questionDef = questions.get(i);
			//if(questionDef.getDataType() == QuestionDef.QTN_TYPE_REPEAT)
			//	selectFirstQuestion(questionDef.getRepeatQtnsDef().getQuestions());
			if(questionDef instanceof GroupDef)
				selectFirstQuestion(((GroupDef)questionDef).getChildren());
			else{
				selectQuestion(questionDef);
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Sets the question for this widget.
	 * 
	 * @param questionDef the question definition object.
	 */
	public void setQuestion(IFormElement questionDef){
		if(questionDef != null)
			fieldHyperlink.setText(questionDef.getText());
		else{
			horizontalPanel.remove(fieldHyperlink);
			horizontalPanel.remove(sgstField);
			
			//Removing and adding of fieldHyperlink is to prevent a wiered bug from
			//happening where focus is taken off, brought back and the hyperlink
			//displays no more text.
			horizontalPanel.add(fieldHyperlink);
			fieldHyperlink.setText("");
		}
	}
	
	public void setForDynamicOptions(boolean forDynamicOptions){
		this.forDynamicOptions = forDynamicOptions;
	}
	
	public void setDynamicQuestionDef(QuestionDef dynamicQuestionDef){
		this.dynamicQuestionDef = dynamicQuestionDef;
	}
}
