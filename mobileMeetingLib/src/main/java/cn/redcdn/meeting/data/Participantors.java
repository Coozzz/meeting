package cn.redcdn.meeting.data;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.menuview.vo.Person;

public abstract class Participantors {
  public enum DataChangeType {
    INC, DEC
  }

  private List<Person> participantors = new ArrayList<Person>();

  public abstract void onPartcipantorChanged(List<Person> list);

  public abstract void onPartcipantorChanged(Person person,
      DataChangeType dataChangeType);

  public void addParticipantors(List<Person> list) {
    participantors.addAll(list);
    onPartcipantorChanged(participantors);
  }

  public void addParticipantor(Person person) {
    if (!participantors.contains(person)) {
      participantors.add(person);
      onPartcipantorChanged(person, DataChangeType.INC);
    }
  }

  public void removeParticipantor(Person person) {
    if (participantors.contains(person)) {
      participantors.remove(person);
      onPartcipantorChanged(person, DataChangeType.DEC);
    }
  }

  public int getSize() {
    return participantors.size();
  }

  public List<Person> getList() {
    return participantors;
  }

  public void clear() {
    participantors.clear();
  }

}
