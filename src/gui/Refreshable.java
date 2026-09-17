package gui;

/** Implemented by any panel that needs to reload its data from the database
 *  when it becomes visible or when related data changes elsewhere. */
public interface Refreshable {
    void refreshData();
}
