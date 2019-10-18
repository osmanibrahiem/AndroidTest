package smartpan.sa.androidtest.model.DBPresenter;

public class DBModel implements DBPresenter {

    private DBView dbView;

    public DBModel(DBView dbView) {
        this.dbView = dbView;
    }

    @Override
    public void add(int x, int y) {


        int result = x + y;


        dbView.onSumResult(result);
    }
}
