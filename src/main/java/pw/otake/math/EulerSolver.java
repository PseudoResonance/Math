package pw.otake.math;

import java.awt.Color;
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
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.StyleContext;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;

public class EulerSolver {
	private static final Dimension DEFAULT_WINDOW_SIZE = new Dimension(500, 400);
	private static final int MAX_STEP_STRING_LENGTH = 4;
	private static final double TARGET_FONT_WIDTH = 7.5;

	private static DecimalFormat df = new DecimalFormat("#.###");
	private static int fontHeight = 0;

	private static JFrame jf;
	private static JPanel panel;
	private static JPanel leftPanel;
	private static JPanel rightPanel;
	private static InputField equation;
	private static InputField initialX;
	private static InputField initialY;
	private static InputField stepSize;
	private static InputField stepCount;
	private static InputField decimals;
	private static JPanel valuesPanel;
	private static JScrollPane valuesScrollPane;
	private static JTable valueTable;
	private static JPanel graphPanel;
	private static XYSeries series = new XYSeries("");
	private static JFreeChart graph;
	private static ChartPanel chartPanel;
	private static DefaultTableModel model;

	private static Expression expression = null;
	private static final Argument x = new Argument("x", 0);
	private static final Argument y = new Argument("y", 0);

	public static void main(String args[]) {
		df.setRoundingMode(RoundingMode.HALF_UP);
		setDefaultFont();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
		}

		jf = new JFrame("Euler Solver");
		panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		leftPanel = new JPanel();
		leftPanel.setLayout(new GridBagLayout());
		rightPanel = new JPanel();
		rightPanel.setLayout(new GridBagLayout());

		equation = new InputField("Equation (dy/dx)", (field) -> {
			if (updateEquation(field.getText())) {
				field.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				return true;
			} else {
				field.setBorder(BorderFactory.createLineBorder(Color.RED));
				return false;
			}
		});
		initialX = new InputField("Initial X Value", (field) -> {
			if (isFieldNumeric(field)) {
				return true;
			}
			return false;
		}, new DocumentFilter() {
			Pattern regEx = Pattern.compile("[\\d-.]*");

			@Override
			public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
				Matcher matcher = regEx.matcher(text);
				if (!matcher.matches())
					return;
				super.replace(fb, offset, length, text, attrs);
			}
		});
		initialY = new InputField("Initial Y Value", (field) -> {
			if (isFieldNumeric(field)) {
				return true;
			}
			return false;
		}, new DocumentFilter() {
			Pattern regEx = Pattern.compile("[\\d-.]*");

			@Override
			public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
				Matcher matcher = regEx.matcher(text);
				if (!matcher.matches())
					return;
				super.replace(fb, offset, length, text, attrs);
			}
		});
		stepSize = new InputField("Step Size (dx)", (field) -> {
			if (isFieldNumeric(field)) {
				return true;
			}
			return false;
		}, new DocumentFilter() {
			Pattern regEx = Pattern.compile("[\\d-.]*");

			@Override
			public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
				Matcher matcher = regEx.matcher(text);
				if (!matcher.matches())
					return;
				super.replace(fb, offset, length, text, attrs);
			}
		});
		stepCount = new InputField("Number of Steps", (field) -> {
			if (isFieldNumeric(field)) {
				return true;
			}
			return false;
		}, new InputFilter() {
			Pattern regEx = Pattern.compile("\\d*");

			@Override
			public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
				Matcher matcher = regEx.matcher(text);
				if (!matcher.matches() || getField().getDocument().getLength() > (MAX_STEP_STRING_LENGTH - 1))
					return;
				super.replace(fb, offset, length, text, attrs);
			}
		});
		decimals = new InputField("Number of Decimals", (field) -> {
			if (isFieldNumeric(field)) {
				String format = "#";
				for (int i = 0; i < Integer.valueOf(field.getText()); i++) {
					if (i == 0)
						format += ".";
					format += "#";
				}
				df = new DecimalFormat(format);
				return true;
			} else
				return false;
		}, new DocumentFilter() {
			Pattern regEx = Pattern.compile("\\d*");

			@Override
			public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
				Matcher matcher = regEx.matcher(text);
				if (!matcher.matches())
					return;
				super.replace(fb, offset, length, text, attrs);
			}
		}, "3");
		((JTextField) decimals.getComponent().getComponent(1)).setBorder(BorderFactory.createLineBorder(Color.BLACK));

		valuesPanel = new JPanel();
		valuesPanel.setLayout(new BoxLayout(valuesPanel, BoxLayout.Y_AXIS));
		valueTable = new JTable();
		newTableModel();
		valuesScrollPane = new JScrollPane(valueTable);
		valueTable.setRowHeight(fontHeight);

		setupGraph();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		panel.add(leftPanel, gbc);
		gbc.gridx = 1;
		panel.add(rightPanel, gbc);
		jf.setSize(DEFAULT_WINDOW_SIZE);
		jf.add(panel);
		setupPanel();
		setupTable();
		jf.setLocationByPlatform(true);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.pack();
		jf.setVisible(true);
	}

	private static void setupPanel() {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.ipadx = 3;
		gbc.ipady = 3;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.insets = new Insets(10, 6, 3, 6);
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		leftPanel.add(equation.getComponent(), gbc);
		gbc.insets = new Insets(3, 6, 3, 6);
		gbc.gridy = 1;
		leftPanel.add(initialX.getComponent(), gbc);
		gbc.gridy = 2;
		leftPanel.add(initialY.getComponent(), gbc);
		gbc.gridy = 3;
		leftPanel.add(stepSize.getComponent(), gbc);
		gbc.gridy = 4;
		leftPanel.add(stepCount.getComponent(), gbc);
		gbc.gridy = 5;
		leftPanel.add(decimals.getComponent(), gbc);
		gbc.insets = new Insets(3, 6, 10, 6);
		gbc.gridy = 6;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		leftPanel.add(valuesPanel, gbc);
	}

	private static void setupTable() {
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
		valuesPanel.setLayout(new GridBagLayout());
		valuesPanel.add(valueLabel, gbc);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 1;
		gbc.weighty = 1;
		valuesPanel.add(valuesScrollPane, gbc);
	}
	
	private static void setupGraph() {
		graphPanel = new JPanel();
		graphPanel.setLayout(new BoxLayout(graphPanel, BoxLayout.Y_AXIS));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		XYSeriesCollection dataset = new XYSeriesCollection();
		graph = ChartFactory.createXYLineChart(null, "x", "y", dataset, PlotOrientation.VERTICAL, false, false, false);
		graph.setAntiAlias(true);
		dataset.addSeries(series);
		chartPanel = new ChartPanel(graph);
		JLabel graphLabel = new JLabel("Graph");
		graphPanel.setLayout(new GridBagLayout());
		graphPanel.add(graphLabel, gbc);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 1;
		gbc.weighty = 1;
		graphPanel.add(chartPanel, gbc);
		gbc.gridy = 0;
		rightPanel.setLayout(new GridBagLayout());
		rightPanel.add(graphPanel, gbc);
	}

	private static void setDefaultFont() {
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
					int newWidth = StyleContext.getDefaultStyleContext().getFontMetrics(newFont).stringWidth("abcdefghijklmnopqrstuvwxyz0123456789 ABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$%^&*()-=_+`~[]{}'\"\\|;:/?.>,<");
					if (newWidth <= targetFontWidth)
						newSize++;
					else
						break;
				}
				fontHeight = StyleContext.getDefaultStyleContext().getFontMetrics(font.deriveFont((float) newSize)).getHeight();
				defaults.put(key, value instanceof FontUIResource ? new FontUIResource(font.getName(), font.getStyle(), newSize) : new Font(font.getName(), font.getStyle(), newSize));
			}
		}
	}

	private static boolean updateEquation(String equation) {
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

	private static void newTableModel() {
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
		series.clear();
	}

	protected static void populateTable() {
		if (equation.isValid() && initialX.isValid() && initialY.isValid() && stepSize.isValid() && stepCount.isValid() && decimals.isValid()) {
			newTableModel();
			double lastX = Double.valueOf(initialX.getValue());
			double lastY = Double.valueOf(initialY.getValue());
			double dx = Double.valueOf(stepSize.getValue());
			model.addRow(new String[] { "0", "0", df.format(lastX), df.format(lastY) });
			for (int i = 1; i <= Integer.valueOf(stepCount.getValue()); i++) {
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
				} else
					if (Double.isFinite(val))
						series.add(lastX, lastY);
				model.addRow(new String[] { "" + i, dyStr, df.format(lastX), lastYStr });
			}
		}
	}
}