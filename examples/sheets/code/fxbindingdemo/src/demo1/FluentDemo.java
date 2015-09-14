package demo1;

import javafx.beans.binding.NumberBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class FluentDemo {

    public static void main( String[] args ) {
        IntegerProperty num1 = new SimpleIntegerProperty( 1 );
        IntegerProperty num2 = new SimpleIntegerProperty( 2 );
        IntegerProperty num3 = new SimpleIntegerProperty( 5 );
        NumberBinding sumMul = num1.add( num2 ).multiply( num3 );
        System.out.println( sumMul.getValue() );
        num1.set( 2 );
        System.err.println( sumMul.getValue() );
    }
}
