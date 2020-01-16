package DTT.rsrpechhulp.Presenter;

import DTT.rsrpechhulp.Model.User;

public class Presenter {

    private User user;

    public Presenter(boolean isPhone){
        user = new User(isPhone);
    }
}
