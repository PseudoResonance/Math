package pw.otake.math;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.StyleContext;

import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;

public class EulerSolver {
	private static DecimalFormat df = new DecimalFormat("#.###");

	private static final Dimension DEFAULT_WINDOW_SIZE = new Dimension(500, 400);
	private static final int MAX_STEP_STRING_LENGTH = 4;
	private static final double TARGET_FONT_WIDTH = 7.5;

	private static int fontHeight = 0;

	static JFrame jf;
	static JPanel panel;
	static GridBagLayout layout;
	static GridBagConstraints layoutConstraints;
	static JPanel equationPanel;
	static JPanel initialXPanel;
	static JPanel initialYPanel;
	static JPanel stepSizePanel;
	static JPanel stepCountPanel;
	static JPanel decimalsPanel;
	static JPanel valuesPanel;
	static JTextField equation;
	static JTextField initialX;
	static JTextField initialY;
	static JTextField stepSize;
	static JTextField stepCount;
	static JTextField decimals;
	static JScrollPane scrollPane;
	static JTable valueTable;
	static DefaultTableModel model;

	private static Expression expression = null;
	private static final Argument x = new Argument("x", 0);
	private static final Argument y = new Argument("y", 0);

	private static boolean validEquation = false;
	private static boolean validInitialX = false;
	private static boolean validInitialY = false;
	private static boolean validStepSize = false;
	private static boolean validStepCount = false;
	private static boolean validDecimals = true;

	public static void main(String args[]) {
		df.setRoundingMode(RoundingMode.HALF_UP);
		int screenDPI = Toolkit.getDefaultToolkit().getScreenResolution();
		int targetFontWidth = (int) (TARGET_FONT_WIDTH * screenDPI);
		UIDefaults defaults = UIManager.getDefaults();
		for (Enumeration<Object> e = defaults.keys(); e.hasMoreElements();) {
			Object key = e.nextElement();
			Object value = defaults.get(key);
			if (value instanceof Font) {
				Font font = (Font) value;
				int newSize = font.getSize();
				while (true) {
					Font newFont = font.deriveFont((float) newSize + 1);
					int newWidth = StyleContext.getDefaultStyleContext().getFontMetrics(newFont).stringWidth(
							"abcdefghijklmnopqrstuvwxyz0123456789 ABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$%^&*()-=_+`~[]{}'\"\\|;:/?.>,<");
					if (newWidth <= targetFontWidth)
						newSize++;
					else
						break;
				}
				fontHeight = StyleContext.getDefaultStyleContext().getFontMetrics(font.deriveFont((float) newSize))
						.getHeight();
				if (value instanceof FontUIResource) {
					defaults.put(key, new FontUIResource(font.getName(), font.getStyle(), newSize));
				} else {
					defaults.put(key, new Font(font.getName(), font.getStyle(), newSize));
				}
			}
		}
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
		}

		jf = new JFrame("Euler Solver");
		panel = new JPanel();
		layout = new GridBagLayout();
		layoutConstraints = new GridBagConstraints();
		panel.setLayout(layout);
		equationPanel = new JPanel();
		equationPanel.setLayout(new BoxLayout(equationPanel, BoxLayout.X_AXIS));
		initialXPanel = new JPanel();
		initialXPanel.setLayout(new BoxLayout(initialXPanel, BoxLayout.X_AXIS));
		initialYPanel = new JPanel();
		initialYPanel.setLayout(new BoxLayout(initialYPanel, BoxLayout.X_AXIS));
		stepSizePanel = new JPanel();
		stepSizePanel.setLayout(new BoxLayout(stepSizePanel, BoxLayout.X_AXIS));
		stepCountPanel = new JPanel();
		stepCountPanel.setLayout(new BoxLayout(stepCountPanel, BoxLayout.X_AXIS));
		decimalsPanel = new JPanel();
		decimalsPanel.setLayout(new BoxLayout(decimalsPanel, BoxLayout.X_AXIS));
		valuesPanel = new JPanel();
		valuesPanel.setLayout(new BoxLayout(valuesPanel, BoxLayout.Y_AXIS));
		equation = new JTextField();
		equation.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				if (testSyntax(equation.getText())) {
					validEquation = true;
					calculate();
					equation.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				} else {
					validEquation = false;
					equation.setBorder(BorderFactory.createLineBorder(Color.RED));
				}
			}

			public void removeUpdate(DocumentEvent e) {
				if (testSyntax(equation.getText())) {
					validEquation = true;
					calculate();
					equation.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				} else {
					validEquation = false;
					equation.setBorder(BorderFactory.createLineBorder(Color.RED));
				}
			}

			public void changedUpdate(DocumentEvent e) {
				if (testSyntax(equation.getText())) {
					validEquation = true;
					calculate();
					equation.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				} else {
					validEquation = false;
					equation.setBorder(BorderFactory.createLineBorder(Color.RED));
				}
			}
		});
		initialX = new JTextField();
		((AbstractDocument) initialX.getDocument()).setDocumentFilter(new DocumentFilter() {
			Pattern regEx = Pattern.compile("[\\d-.]*");

			@Override
			public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
					throws BadLocationException {
				Matcher matcher = regEx.matcher(text);
				if (!matcher.matches()) {
					return;
				}
				super.replace(fb, offset, length, text, attrs);
			}
		});
		initialX.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				validInitialX = isFieldNumeric(initialX) ? true : false;
				calculate();
			}

			public void removeUpdate(DocumentEvent e) {
				validInitialX = isFieldNumeric(initialX) ? true : false;
				calculate();
			}

			public void changedUpdate(DocumentEvent e) {
				validInitialX = isFieldNumeric(initialX) ? true : false;
				calculate();
			}
		});
		initialY = new JTextField();
		((AbstractDocument) initialY.getDocument()).setDocumentFilter(new DocumentFilter() {
			Pattern regEx = Pattern.compile("[\\d-.]*");

			@Override
			public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
					throws BadLocationException {
				Matcher matcher = regEx.matcher(text);
				if (!matcher.matches()) {
					return;
				}
				super.replace(fb, offset, length, text, attrs);
			}
		});
		initialY.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				validInitialY = isFieldNumeric(initialY) ? true : false;
				calculate();
			}

			public void removeUpdate(DocumentEvent e) {
				validInitialY = isFieldNumeric(initialY) ? true : false;
				calculate();
			}

			public void changedUpdate(DocumentEvent e) {
				validInitialY = isFieldNumeric(initialY) ? true : false;
				calculate();
			}
		});
		stepSize = new JTextField();
		((AbstractDocument) stepSize.getDocument()).setDocumentFilter(new DocumentFilter() {
			Pattern regEx = Pattern.compile("[\\d-.]*");

			@Override
			public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
					throws BadLocationException {
				Matcher matcher = regEx.matcher(text);
				if (!matcher.matches()) {
					return;
				}
				super.replace(fb, offset, length, text, attrs);
			}
		});
		stepSize.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				validStepSize = isFieldNumeric(stepSize) ? true : false;
				calculate();
			}

			public void removeUpdate(DocumentEvent e) {
				validStepSize = isFieldNumeric(stepSize) ? true : false;
				calculate();
			}

			public void changedUpdate(DocumentEvent e) {
				validStepSize = isFieldNumeric(stepSize) ? true : false;
				calculate();
			}
		});
		stepCount = new JTextField();
		((AbstractDocument) stepCount.getDocument()).setDocumentFilter(new DocumentFilter() {
			Pattern regEx = Pattern.compile("\\d*");

			@Override
			public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
					throws BadLocationException {
				Matcher matcher = regEx.matcher(text);
				if (!matcher.matches() || stepCount.getDocument().getLength() > (MAX_STEP_STRING_LENGTH - 1)) {
					return;
				}
				super.replace(fb, offset, length, text, attrs);
			}
		});
		stepCount.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				validStepCount = isFieldNumeric(stepCount) ? true : false;
				calculate();
			}

			public void removeUpdate(DocumentEvent e) {
				validStepCount = isFieldNumeric(stepCount) ? true : false;
				calculate();
			}

			public void changedUpdate(DocumentEvent e) {
				validStepCount = isFieldNumeric(stepCount) ? true : false;
				calculate();
			}
		});
		decimals = new JTextField();
		decimals.setText("3");
		decimals.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		((AbstractDocument) decimals.getDocument()).setDocumentFilter(new DocumentFilter() {
			Pattern regEx = Pattern.compile("\\d*");

			@Override
			public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
					throws BadLocationException {
				Matcher matcher = regEx.matcher(text);
				if (!matcher.matches()) {
					return;
				}
				super.replace(fb, offset, length, text, attrs);
			}
		});
		decimals.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				if (isFieldNumeric(decimals)) {
					validDecimals = true;
					String format = "#";
					for (int i = 0; i < Integer.valueOf(decimals.getText()); i++) {
						if (i == 0)
							format += ".";
						format += "#";
					}
					df = new DecimalFormat(format);
					calculate();
				} else
					validDecimals = false;
			}

			public void removeUpdate(DocumentEvent e) {
				if (isFieldNumeric(decimals)) {
					validDecimals = true;
					String format = "#.";
					for (int i = 0; i < Integer.valueOf(decimals.getText()); i++) {
						format += "#";
					}
					df = new DecimalFormat(format);
					calculate();
				} else
					validDecimals = false;
			}

			public void changedUpdate(DocumentEvent e) {
				if (isFieldNumeric(decimals)) {
					validDecimals = true;
					String format = "#.";
					for (int i = 0; i < Integer.valueOf(decimals.getText()); i++) {
						format += "#";
					}
					df = new DecimalFormat(format);
					calculate();
				} else
					validDecimals = false;
			}
		});
		valueTable = new JTable();
		makeNewModel();
		scrollPane = new JScrollPane(valueTable);
		valueTable.setRowHeight(fontHeight);

		jf.setSize(DEFAULT_WINDOW_SIZE);
		jf.add(panel);
		layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
		layoutConstraints.gridx = 0;
		layoutConstraints.gridy = 0;
		layoutConstraints.ipadx = 3;
		layoutConstraints.ipady = 3;
		layoutConstraints.weightx = 1;
		layoutConstraints.weighty = 0;
		layoutConstraints.insets = new Insets(10, 6, 3, 6);
		layoutConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		panel.add(equationPanel, layoutConstraints);
		layoutConstraints.insets = new Insets(3, 6, 3, 6);
		layoutConstraints.gridy = 1;
		panel.add(initialXPanel, layoutConstraints);
		layoutConstraints.gridy = 2;
		panel.add(initialYPanel, layoutConstraints);
		layoutConstraints.gridy = 3;
		panel.add(stepSizePanel, layoutConstraints);
		layoutConstraints.gridy = 4;
		panel.add(stepCountPanel, layoutConstraints);
		layoutConstraints.gridy = 5;
		panel.add(decimalsPanel, layoutConstraints);
		layoutConstraints.insets = new Insets(3, 6, 10, 6);
		layoutConstraints.gridy = 6;
		layoutConstraints.weighty = 1;
		layoutConstraints.fill = GridBagConstraints.BOTH;
		panel.add(valuesPanel, layoutConstraints);

		equationPanel.add(new JLabel("Equation (dy/dx) "));
		equationPanel.add(equation);
		initialXPanel.add(new JLabel("Initial X Value "));
		initialXPanel.add(initialX);
		initialYPanel.add(new JLabel("Initial Y Value "));
		initialYPanel.add(initialY);
		stepSizePanel.add(new JLabel("Step Size (dx) "));
		stepSizePanel.add(stepSize);
		stepCountPanel.add(new JLabel("Number of Steps "));
		stepCountPanel.add(stepCount);
		decimalsPanel.add(new JLabel("Number of Decimals "));
		decimalsPanel.add(decimals);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		JLabel valueLabel = new JLabel("Values");
		valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		valueLabel.setBackground(Color.RED);
		valuesPanel.setLayout(new GridBagLayout());
		valuesPanel.add(valueLabel, gbc);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 1;
		gbc.weighty = 1;
		valuesPanel.add(scrollPane, gbc);
		jf.setLocationByPlatform(true);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.pack();
		jf.setVisible(true);
	}

	private static boolean testSyntax(String equation) {
		Expression exp = new Expression(equation);
		exp.addArguments(x);
		exp.addArguments(y);
		boolean test = exp.checkSyntax();
		if (test)
			expression = exp;
		return test;
	}

	private static boolean isValidNumeric(String test) {
		try {
			Double.valueOf(test);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private static boolean isFieldNumeric(JTextField field) {
		if (isValidNumeric(field.getText())) {
			field.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			return true;
		} else {
			field.setBorder(BorderFactory.createLineBorder(Color.RED));
			return false;
		}
	}

	private static void makeNewModel() {
		model = new DefaultTableModel() {
			private static final long serialVersionUID = 193961864905348635L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		model.addColumn("Step");
		model.addColumn("dy");
		model.addColumn("x Value");
		model.addColumn("y Value");
		valueTable.setModel(model);
	}

	private static void calculate() {
		if (validEquation && validInitialX && validInitialY && validStepSize && validStepCount && validDecimals) {
			makeNewModel();
			double lastX = Double.valueOf(initialX.getText());
			double lastY = Double.valueOf(initialY.getText());
			double dx = Double.valueOf(stepSize.getText());
			model.addRow(new String[] { "0", "0", df.format(lastX), df.format(lastY) });
			for (int i = 1; i <= Integer.valueOf(stepCount.getText()); i++) {
				x.setArgumentValue(lastX);
				y.setArgumentValue(lastY);
				double val = expression.calculate();
				double dy = val * dx;
				lastX += dx;
				lastY += dy;
				String dyStr = df.format(dy);
				String lastYStr = df.format(lastY);
				if (Double.isNaN(val)) {
					dyStr = "NaN";
					lastYStr = "NaN";
				}
				model.addRow(new String[] { "" + i, dyStr, df.format(lastX), lastYStr });
			}
		}
	}
}