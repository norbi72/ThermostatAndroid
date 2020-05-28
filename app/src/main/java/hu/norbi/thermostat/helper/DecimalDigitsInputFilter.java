package hu.norbi.thermostat.helper;

import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DecimalDigitsInputFilter implements InputFilter {
    Pattern mPattern;

    public DecimalDigitsInputFilter(int digitsBeforeDecimalSeparator, int digitsAfterDecimalSeparator) {
        mPattern = Pattern.compile("^\\d{1," + digitsBeforeDecimalSeparator + "}(\\.\\d{0," + digitsAfterDecimalSeparator + "})?$");
//        mPattern = Pattern.compile("[0-9]{0,2}((\\.[0-9]{0,1})?)|(\\.)?");
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
System.out.println(source);
        CharSequence match = TextUtils.concat(dest.subSequence(0, dstart), source.subSequence(start, end), dest.subSequence(dend, dest.length()));
        Matcher matcher = mPattern.matcher(match);
        if (!matcher.matches())
            return "";
        return null;
    }
}
