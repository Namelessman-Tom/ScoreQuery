// src/main/java/top/namelessman/studentscorequery/dto/AdminQueryParam.java
package top.namelessman.studentscorequery.dto;

import java.util.List;

public class AdminQueryParam {
    private String password;
    private List<String> classNames;
    private Boolean isChecked;
    private Boolean hasFeedback;

    // Getter 和 Setter 方法
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getClassNames() {
        return classNames;
    }

    public void setClassNames(List<String> classNames) {
        this.classNames = classNames;
    }

    public Boolean getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(Boolean checked) {
        isChecked = checked;
    }

    public Boolean getHasFeedback() {
        return hasFeedback;
    }

    public void setHasFeedback(Boolean hasFeedback) {
        this.hasFeedback = hasFeedback;
    }

    @Override
    public String toString() {
        return "AdminQueryParam{" +
               "password='" + password + '\'' +
               ", classNames=" + classNames +
               ", isChecked=" + isChecked +
               ", hasFeedback=" + hasFeedback +
               '}';
    }
}
