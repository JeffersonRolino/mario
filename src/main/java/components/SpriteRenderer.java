package components;

import cosmos.Component;

public class SpriteRenderer extends Component {
    private boolean firstTime = false;

    @Override
    public void update(float dt) {
        if(!firstTime){
            System.out.println("I am updating...");
            firstTime = true;
        }
    }

    @Override
    public void start(){
        System.out.println("I am starting...");
    }
}
