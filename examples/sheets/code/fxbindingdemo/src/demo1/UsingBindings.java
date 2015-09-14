package demo1;

import static javafx.beans.binding.Bindings.*;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class UsingBindings {

    public static void main( String[] args ) {
        IntegerProperty num1 = new SimpleIntegerProperty( 1 );
        IntegerProperty num2 = new SimpleIntegerProperty( 2 );
        IntegerProperty num3 = new SimpleIntegerProperty( 5 );
        // note the use of static import
        NumberBinding sum = multiply( num3, add( num1, num2 ) );
        System.out.println( sum.getValue() );
        num1.setValue( 2 );
        System.err.println( sum.getValue() );
    }
}
