package demo1;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class UsingChangeListener {

    public static void main( String[] args ) {

        Bill electricBill = new Bill();
        electricBill.amountDueProperty().addListener(
                new ChangeListener<Number>() {

                    @Override
                    public void changed(
                            ObservableValue<? extends Number> observable,
                            Number oldValue, Number newValue ) {
                                System.out.println(
                                        "Electric bill has changed! to "
                                        + newValue );
                            }
                } );
        // lambda
        electricBill.amountDueProperty().addListener( (
                ObservableValue<? extends Number> observable,
                Number oldValue, Number newValue ) -> {
            System.out.println( "Electric bill has changed! to " + newValue );
        } );

        electricBill.setAmountDue( 100.00 );

    }
}
