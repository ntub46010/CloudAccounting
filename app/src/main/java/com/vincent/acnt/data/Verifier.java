package com.vincent.acnt.data;

import android.content.Context;
import android.support.design.widget.TextInputLayout;

import com.vincent.acnt.R;
import com.vincent.acnt.entity.Entry;
import com.vincent.acnt.entity.Subject;
import com.vincent.acnt.entity.UserProfileRequest;

import java.util.List;
import java.util.regex.Pattern;

public class Verifier {
    private Context c;
    private String ptnWord = "[a-zA-Z0-9]{%s,%s}";
    private String ptnChineseWord = "[\\u4e00-\\u9fa5a-zA-Z_0-9\\u3002\\uff1b\\uff0c\\uff1a\\u201c\\u201d\\uff08\\uff09\\u3001\\uff1f\\u300a\\u300b^\\x00-\\xff]{%s,%s}";
    private String ptnNumber = "[0-9]{%s,%s}";
    private String ptnEmail = "^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$";

    private String ptnPassword = String.format(ptnWord, "8", "20");
    private String ptnSubjectNo = String.format(ptnNumber, "3", "3");
    private String ptnSubjectName = String.format(ptnChineseWord, "1", "30");
    private String ptnMemo = String.format(ptnChineseWord, "0", "100");

    public Verifier(Context context) {
        this.c = context;
    }

    public String chkSubjectNo(String s) {
        if (Pattern.matches(ptnSubjectNo, s) && !s.substring(2, 3).equals("0"))
            return "";
        else
            return c.getString(R.string.chk_format_wrong, "編號");
    }

    public String chkSubjectName(String s) {
        if (Pattern.matches(ptnSubjectName, s))
            return "";
        else
            return c.getString(R.string.chk_format_wrong, "科目名稱");
    }

    public String verifySubject(Subject subject, ObjectMapTable<Long, Subject> subjectTable, int mode) {
        StringBuilder errMsg = new StringBuilder(64);

        errMsg.append(checkSubjectNo(subject.getNo(), subjectTable, mode));
        errMsg.append(checkSubjectName(subject.getName(), subjectTable));
        errMsg.append(checkSubjectAmount(subject));

        return errMsg.toString();
    }

    private String checkSubjectNo(String subjectNo, ObjectMapTable<Long, Subject> subjectTable, int mode) {
        StringBuilder sb = new StringBuilder();

        if (subjectNo.substring(0, 1).equals("0")) {
            sb.append("科目類別未選擇\n");

        } else {
            if (mode == Constant.MODE_CREATE) {
                if (subjectTable.existByProperty(Constant.PRO_SUBJECT_NO, subjectNo)) {
                    sb.append("科目編號").append(subjectNo).append("已被使用\n");
                } else {
                    if (!Pattern.matches(ptnSubjectNo, subjectNo) || subjectNo.substring(2, 3).equals("0")) {
                        return c.getString(R.string.chk_format_wrong, "編號");
                    }
                }
            }
        }

        return sb.toString();
    }

    private String checkSubjectName(String subjectName, ObjectMapTable<Long, Subject> subjectTable) {
        StringBuilder sb = new StringBuilder();

        if (subjectTable.existByProperty(Constant.PRO_SUBJECT_NO, subjectName)) {
            sb.append("科目名稱").append(subjectName).append("已被使用\n");
        } else if (!Pattern.matches(ptnSubjectName, subjectName)) {
            sb.append(c.getString(R.string.chk_format_wrong, "科目名稱"));
        }

        return sb.toString();
    }

    private String checkSubjectAmount(Subject subject) {
        if (subject.getCredit() != 0 && subject.getDebit() != 0) {
             return "只能在借貸其中一方輸入金額\n";
        }

        return "";
    }

    public String verifyEntry(Entry entry, ObjectMapTable<Long, Subject> subjectTable) {
        StringBuilder errMsg = new StringBuilder(64);

        errMsg.append(checkEntryDate(entry.getDate()));
        errMsg.append(checkMemo(entry.getMemo()));
        errMsg.append(checkPs(entry.getPs()));
        errMsg.append(checkBalance(entry));
        errMsg.append(checkEntrySubjectExist(entry.getSubjects(), subjectTable));

        return errMsg.toString();
    }

    private String checkEntryDate(int date) {
        if (date == 0) {
            return "日期未輸入\n";
        }

        return "";
    }

    private String checkMemo(String memo) {
        if (Pattern.matches(ptnMemo, memo))
            return "";
        else
            return c.getString(R.string.chk_max_words, "摘要", "100");
    }

    private String checkPs(String ps) {
        if (Pattern.matches(ptnMemo, ps))
            return "";
        else
            return c.getString(R.string.chk_max_words, "備註", "100");
    }

    private String checkBalance(Entry entry) {
        if (entry.calDifference() != 0) {
            return "借貸金額不平衡\n";
        }

        return "";
    }

    private String checkEntrySubjectExist(List<Subject> subjects, ObjectMapTable<Long, Subject> subjectTable) {
        StringBuilder sb = new StringBuilder("");

        Subject expectSubject, subject;
        for (int i = 0, len = subjects.size(); i < len; i++) {
            expectSubject = subjects.get(i);

            subject = subjectTable.findFirstByProperty(Constant.PRO_NAME, expectSubject.getName());
            if (subject == null) {
                sb.append(expectSubject.getName()).append("、");
            }
        }

        if (sb.length() > 0) {
            sb.insert(0, "以下科目不存在：");
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    public boolean verifyUserProfile(UserProfileRequest request, TextInputLayout tilNickName, TextInputLayout tilEmail, TextInputLayout tilPwd1, TextInputLayout tilPwd2, TextInputLayout tilPwd3) {
        boolean isNotValid = false;

        tilNickName.setError(checkNickName(request.getNickName()));
        tilEmail.setError(checkEmail(request.getEmail()));

        if (tilPwd3 == null) {
            tilPwd1.setError(checkPassword(request.getNewPwd()));
            tilPwd2.setError(checkPasswordConfirm(request.getNewPwd(), request.getNewPwdConfirm()));

            isNotValid = tilNickName.getError() != null || tilEmail.getError() != null || tilPwd1.getError() != null || tilPwd2.getError() != null;
        } else {
            String newPwd = request.getNewPwd();
            String newPwdConfirm = request.getNewPwdConfirm();

            if (!Utility.isEmptyString(newPwd) || !Utility.isEmptyString(newPwdConfirm)) {
                tilPwd2.setError(checkPassword(newPwd));
                tilPwd3.setError(checkPasswordConfirm(newPwd, newPwdConfirm));
            }

            isNotValid = tilNickName.getError() != null || tilEmail.getError() != null || tilPwd2.getError() != null || tilPwd3.getError() != null;
        }

        return isNotValid;
    }

    private String checkNickName(String s) {
        if (Utility.isEmptyString(s))
            return "暱稱未填寫";
        else if (s.length() > 20)
            return "暱稱不可超過20字";
        else
            return null;
    }

    private String checkEmail(String s) {
        if (Pattern.matches(ptnEmail, s))
            return null;
        else
            return c.getString(R.string.chk_format_wrong, "Email");
    }

    private String checkPassword(String s) {
        if (Pattern.matches(ptnPassword, s))
            return null;
        else
            return "密碼需為8~20位英數字";
    }

    private String checkPasswordConfirm(String pwd1, String pwd2) {
        if (pwd1.equals(pwd2))
            return null;
        else
            return "確認密碼不相符";
    }



}
