package org.example.table;

import jakarta.persistence.*;

@Entity
@Table(name = "groupTG")
public class GroupTG {

    @Id
    @Column(name = "groupID")
    private long groupID;

    @Column(name = "size")
    private int size = 0;

    public long getGroupID() {
        return groupID;
    }

    public void setGroupID(long groupID) {
        this.groupID = groupID;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
