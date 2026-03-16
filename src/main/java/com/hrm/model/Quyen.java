package com.hrm.model;

public class Quyen {
    private String id;
    private String tenQuyen;
    private String nhomQuyen;
    private String moTa;
    /** Phạm vi dữ liệu — chỉ có giá trị khi được nạp từ VAITRO_QUYEN (kèm role). */
    private DataScope phamVi;
    /** TRUE nếu quyền có ý nghĩa scope (VIEW/APPROVE/REVIEW...); FALSE nếu là hành động nhị phân (CREATE/MANAGE...). */
    private boolean coPhamVi = true;

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
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public DataScope getPhamVi() { return phamVi; }
    public void setPhamVi(DataScope phamVi) { this.phamVi = phamVi; }
    public boolean isCoPhamVi() { return coPhamVi; }
    public void setCoPhamVi(boolean coPhamVi) { this.coPhamVi = coPhamVi; }
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
