package physics;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * Simple Second Order system to demo JavaFX properties . This system models a
 * mass, damper spring system with viscous damping and a linear spring.
 *
 * Newtonian laws apply: The mass is accelerated with a value proportional to
 * the resulting force and inversely proportional to the (constant mass).
 *
 * Th
 *
 * @author hom
 */
public class SecondOrderSystem {

    private final double mass;
    private final double damping;
    private final double spring;

    private final DoubleProperty position = new SimpleDoubleProperty( 0 );
    private final DoubleProperty speed = new SimpleDoubleProperty( 0 );
    private final DoubleProperty acceleration = new SimpleDoubleProperty( 0 );
    private final DoubleProperty externalForce = new SimpleDoubleProperty( 0 );
    private long lastTime = 0;

    private double timeDelta() {
        long now = System.nanoTime();
        double result = ( now - lastTime ) * 1E-9d;
        lastTime = now;
        return result;
    }

    public SecondOrderSystem( double mass, double damping, double spring ) {
        this.mass = mass;
        this.damping = damping;
        this.spring = spring;
        lastTime = System.nanoTime();
        speed.bind( acceleration );

    }

    double getPosition() {
        double td = timeDelta();
        position.add( speed.multiply( td ) );
        return position.get();
    }

}
