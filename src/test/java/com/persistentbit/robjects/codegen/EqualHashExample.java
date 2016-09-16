package com.persistentbit.robjects.codegen;

/**
 * Created by petermuys on 16/09/16.
 */
public class EqualHashExample {
    private Float aFloat;
    private float afloat;
    private Double aDouble;
    private double adouble;
    private String aString;
    private byte abyte;
    private Byte aByte;
    private Short aShort;
    private short ashort;

    public EqualHashExample() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EqualHashExample that = (EqualHashExample) o;

        if (Float.compare(that.afloat, afloat) != 0) return false;
        if (Double.compare(that.adouble, adouble) != 0) return false;
        if (abyte != that.abyte) return false;
        if (ashort != that.ashort) return false;
        if (aFloat != null ? !aFloat.equals(that.aFloat) : that.aFloat != null) return false;
        if (aDouble != null ? !aDouble.equals(that.aDouble) : that.aDouble != null) return false;
        if (aString != null ? !aString.equals(that.aString) : that.aString != null) return false;
        if (aByte != null ? !aByte.equals(that.aByte) : that.aByte != null) return false;
        return aShort != null ? aShort.equals(that.aShort) : that.aShort == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = aFloat != null ? aFloat.hashCode() : 0;
        result = 31 * result + (afloat != +0.0f ? Float.floatToIntBits(afloat) : 0);
        result = 31 * result + (aDouble != null ? aDouble.hashCode() : 0);
        temp = Double.doubleToLongBits(adouble);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (aString != null ? aString.hashCode() : 0);
        result = 31 * result + (int) abyte;
        result = 31 * result + (aByte != null ? aByte.hashCode() : 0);
        result = 31 * result + (aShort != null ? aShort.hashCode() : 0);
        result = 31 * result + (int) ashort;
        return result;
    }
}
