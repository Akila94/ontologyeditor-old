package hello.bean;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.List;

public class MainChanges {
    private int current=1;
    private int rowCount = 10;
    @JsonManagedReference
    private List<MainChangeCol> rows;
    private int total;

    public MainChanges() {
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public List<MainChangeCol> getRows() {
        return rows;
    }

    public void setRows(List<MainChangeCol> rows) {
        this.rows = rows;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
