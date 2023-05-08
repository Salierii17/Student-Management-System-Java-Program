package main;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilCalendarModel;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

public class Example {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Date Picker Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());

        UtilCalendarModel model = new UtilCalendarModel();
        Properties properties = new Properties();
        properties.put("text.today", "Today");
        properties.put("text.month", "Month");
        properties.put("text.year", "Year");

        JDatePanelImpl datePanel = new JDatePanelImpl(model, properties);
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());

        panel.add(datePicker, BorderLayout.CENTER);

        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    private static class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {
        private static final String DATE_PATTERN = "yyyy-MM-dd";

        @Override
        public Object stringToValue(String text) throws ParseException {
            return null;
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            if (value != null) {
                Calendar calendar = (Calendar) value;
                SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN);
                return format.format(calendar.getTime());
            }

            return "";
        }
    }
}