package top.namelessman.studentscorequery.dto;

import java.util.List;

public class AdminQueryParam {
    private String password;
    private List<String> teachingClassNames;
    private Boolean isChecked;
    private Boolean hasFeedback;

    // Getter 和 Setter 方法
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getTeachingClassNames() {
        return teachingClassNames;
    }

    public void setTeachingClassNames(List<String> teachingClassNames) {
        this.teachingClassNames = teachingClassNames;
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
                ", teachingClassNames=" + teachingClassNames +
                ", isChecked=" + isChecked +
                ", hasFeedback=" + hasFeedback +
                '}';
    }
}
