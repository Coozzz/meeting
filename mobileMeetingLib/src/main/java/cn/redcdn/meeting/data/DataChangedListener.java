package cn.redcdn.meeting.data;

import java.util.List;

import cn.redcdn.menuview.vo.Person;

public interface DataChangedListener {
  public void onDataChanged(List<Person> dataList, List<Person> participatorList);
}
