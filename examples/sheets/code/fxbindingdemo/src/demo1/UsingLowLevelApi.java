package demo1;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class UsingLowLevelApi {

    public static void main( String[] args ) {

        final DoubleProperty a = new SimpleDoubleProperty( 1 );
        final DoubleProperty b = new SimpleDoubleProperty( 2 );
        final DoubleProperty c = new SimpleDoubleProperty( 3 );
        final DoubleProperty d = new SimpleDoubleProperty( 4 );

        DoubleBinding db = new DoubleBinding() {

            {
                super.bind( a, b, c, d );
            }

            @Override
            protected double computeValue() {
                return ( a.get() * b.get() ) + ( c.get() * d.get() );
            }
        };

        System.out.println( db.get() );
        b.set( 3 );
        System.err.println( db.get() );
    }
}
