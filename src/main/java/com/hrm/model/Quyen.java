package com.hrm.model;

public class Quyen {
    private String id;
    private String tenQuyen;
    private String nhomQuyen;

    public Quyen() {}

    public Quyen(String id, String tenQuyen, String nhomQuyen) {
        this.id = id;
        this.tenQuyen = tenQuyen;
        this.nhomQuyen = nhomQuyen;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenQuyen() { return tenQuyen; }
    public void setTenQuyen(String tenQuyen) { this.tenQuyen = tenQuyen; }
    public String getNhomQuyen() { return nhomQuyen; }
    public void setNhomQuyen(String nhomQuyen) { this.nhomQuyen = nhomQuyen; }
    /** Alias for getId() */
    public String getCode() { return id; }
    /** Alias for getTenQuyen() */
    public String getName() { return tenQuyen; }
    /** Alias for getNhomQuyen() */
    public String getModule() { return nhomQuyen; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Quyen that = (Quyen) obj;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return id != null ? id.hashCode() : 0; }

    @Override
    public String toString() { return tenQuyen + " (" + id + ")"; }
}
