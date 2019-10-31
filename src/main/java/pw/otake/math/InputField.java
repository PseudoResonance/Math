package pw.otake.math;

import java.util.concurrent.Callable;
import java.util.function.Function;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DocumentFilter;

public class InputField {

	private String title;

	private JPanel panel;
	private JLabel label;
	private JTextField text;

	private boolean valid;

	public InputField(String title, Function<JTextField, Boolean> validityCheck) {
		this.title = title;

		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		label = new JLabel(title + " ");
		panel.add(label);
		text = new JTextField();
		addDocumentListener(text, validityCheck);
		panel.add(text);
		valid = false;
	}

	public InputField(String title, Function<JTextField, Boolean> validityCheck, DocumentFilter filter) {
		this.title = title;

		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		label = new JLabel(title + " ");
		panel.add(label);
		text = new JTextField();
		addDocumentListener(text, validityCheck);
		((AbstractDocument) text.getDocument()).setDocumentFilter(filter);
		if (filter instanceof InputFilter)
			((InputFilter) filter).setField(text);
		panel.add(text);
		valid = false;
	}

	public InputField(String title, Function<JTextField, Boolean> validityCheck, String defaultValue) {
		this.title = title;

		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		label = new JLabel(title + " ");
		panel.add(label);
		text = new JTextField();
		addDocumentListener(text, validityCheck);
		text.setText(defaultValue);
		panel.add(text);
		valid = true;
	}

	public InputField(String title, Function<JTextField, Boolean> validityCheck, DocumentFilter filter, String defaultValue) {
		this.title = title;

		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		label = new JLabel(title + " ");
		panel.add(label);
		text = new JTextField();
		addDocumentListener(text, validityCheck);
		((AbstractDocument) text.getDocument()).setDocumentFilter(filter);
		if (filter instanceof InputFilter)
			((InputFilter) filter).setField(text);
		text.setText(defaultValue);
		panel.add(text);
		valid = true;
	}

	public InputField(String title, Callable<Boolean> validityCheck) {
		this.title = title;

		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		label = new JLabel(title + " ");
		panel.add(label);
		text = new JTextField();
		addDocumentListener(text, validityCheck);
		panel.add(text);
		valid = false;
	}

	public InputField(String title, Callable<Boolean> validityCheck, DocumentFilter filter) {
		this.title = title;

		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		label = new JLabel(title + " ");
		panel.add(label);
		text = new JTextField();
		addDocumentListener(text, validityCheck);
		((AbstractDocument) text.getDocument()).setDocumentFilter(filter);
		if (filter instanceof InputFilter)
			((InputFilter) filter).setField(text);
		panel.add(text);
		valid = false;
	}

	public InputField(String title, Callable<Boolean> validityCheck, String defaultValue) {
		this.title = title;

		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		label = new JLabel(title + " ");
		panel.add(label);
		text = new JTextField();
		addDocumentListener(text, validityCheck);
		text.setText(defaultValue);
		panel.add(text);
		valid = true;
	}

	public InputField(String title, Callable<Boolean> validityCheck, DocumentFilter filter, String defaultValue) {
		this.title = title;

		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		label = new JLabel(title + " ");
		panel.add(label);
		text = new JTextField();
		addDocumentListener(text, validityCheck);
		((AbstractDocument) text.getDocument()).setDocumentFilter(filter);
		if (filter instanceof InputFilter)
			((InputFilter) filter).setField(text);
		text.setText(defaultValue);
		panel.add(text);
		valid = true;
	}

	public String getTitle() {
		return title;
	}

	public boolean isValid() {
		return valid;
	}

	public String getValue() {
		return text.getText();
	}

	public JPanel getComponent() {
		return panel;
	}

	private void addDocumentListener(JTextField field, Function<JTextField, Boolean> method) {
		field.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				try {
					valid = method.apply(text);
					if (valid)
						EulerSolver.populateTable();
				} catch (Exception e1) {
				}
			}

			public void removeUpdate(DocumentEvent e) {
				try {
					valid = method.apply(text);
					if (valid)
						EulerSolver.populateTable();
				} catch (Exception e1) {
				}
			}

			public void changedUpdate(DocumentEvent e) {
				try {
					valid = method.apply(text);
					if (valid)
						EulerSolver.populateTable();
				} catch (Exception e1) {
				}
			}
		});
	}

	private void addDocumentListener(JTextField field, Callable<Boolean> method) {
		field.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				try {
					valid = method.call();
					if (valid)
						EulerSolver.populateTable();
				} catch (Exception e1) {
				}
			}

			public void removeUpdate(DocumentEvent e) {
				try {
					valid = method.call();
					if (valid)
						EulerSolver.populateTable();
				} catch (Exception e1) {
				}
			}

			public void changedUpdate(DocumentEvent e) {
				try {
					valid = method.call();
					if (valid)
						EulerSolver.populateTable();
				} catch (Exception e1) {
				}
			}
		});
	}

}

class InputFilter extends DocumentFilter {

	private JTextField field = null;

	public void setField(JTextField field) {
		this.field = field;
	}

	protected JTextField getField() {
		return field;
	}
}
