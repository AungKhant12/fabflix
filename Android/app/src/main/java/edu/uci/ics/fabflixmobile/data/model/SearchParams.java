package edu.uci.ics.fabflixmobile.data.model;

public class SearchParams {
    String title = "";
    int pageNum = 1;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPageNum() {
        return String.valueOf(pageNum);
    }

    public void incrementPageNum() {
        this.pageNum += 1;
    }

    public void decrementPageNum() {
        if (this.pageNum > 1){
            this.pageNum -= 1;
        }
    }
}
