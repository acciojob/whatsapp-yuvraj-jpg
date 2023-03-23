package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PutMapping;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }
    public String createUser(String name , String mobile) throws Exception {
        if(userMobile.contains(mobile)){
            throw new Exception("User already exists");
        }
        userMobile.add(mobile);
        User user = new User(name,mobile);
        return  "SUCCESS";

    }
    // The list contains at least 2 users where the first user is the admin. A group has exactly one admin.
    // If there are only 2 users, the group is a personal chat and the group name should be kept as the name of the second user(other than admin)
    // If there are 2+ users, the name of group should be "Group count". For example, the name of first group would be "Group 1", second would be "Group 2" and so on.
    // Note that a personal chat is not considered a group and the count is not updated for personal chats.
    // If group is successfully created, return group.

    //For example: Consider userList1 = {Alex, Bob, Charlie}, userList2 = {Dan, Evan}, userList3 = {Felix, Graham, Hugh}.
    //If createGroup is called for these userLists in the same order, their group names would be "Group 1", "Evan", and "Group 2" respectively.

    public Group createGroup(List<User> users){
        String grpName="";
        if(users.size()>2){
            this.customGroupCount++;
            grpName="Group "+this.customGroupCount;
        }
        else{
            grpName=users.get(1).getName();
        }
        Group group = new Group();
        group.setName(grpName);
        group.setNumberOfParticipants(users.size());
        groupUserMap.put(group,users);
        adminMap.put(group,users.get(0));
        groupMessageMap.put(group,new ArrayList<>());
        return group;
    }
    public int createMessage(String content){
        // The 'i^th' created message has message id 'i'.
        // Return the message id.
        this.messageId++;
        Message message = new Message();
        message.setId(this.messageId);
        message.setContent(content);
        return this.messageId;
    }
    public int sendMessage(Message message, User sender, Group group) throws Exception{
        if(groupUserMap.containsKey(group)){
            if(groupUserMap.get(group).contains(sender)){
                groupMessageMap.get(group).add(message);
                return groupMessageMap.get(group).size();
            }else {
                throw new Exception("You are not allowed to send message");
            }
        }
        else{
            throw new Exception("Group does not exist");
        }
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        //If the message is sent successfully, return the final number of messages in that group.
    }
    public String changeAdmin(User approver, User user, Group group) throws Exception{
        if(!groupUserMap.containsKey(group)) throw new Exception("Group does not exist");
        if(!adminMap.get(group).equals(approver))  throw new Exception("Approver does not have rights");

        Boolean ismember=false;
        List<User> userList = groupUserMap.get(group);
        for(User user1 : userList){
            if(user1.equals(user)){
                ismember = true;
                break;
            }
        }
        if(ismember==false) throw new Exception("User is not a participant");
        adminMap.put(group,user);
        return  "SUCCESS";

        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS". Note that at one time there is only one admin and the admin rights are transferred from approver to user.
    }
    public int removeUser(User user) throws Exception{
        boolean userFound = false;
        Group userGroup = null;
        for(Group group : groupUserMap.keySet()){
            if(groupUserMap.get(group).contains(user)){
                userGroup = group;
                if(Objects.equals(adminMap.get(userGroup).getName(), user.getName())){
                    throw new Exception("Cannot remove admin");
                }
                userFound = true;
                break;
            }
        }
        //This is a bonus problem and does not contains any marks
        //A user belongs to exactly one group
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)
        if(userFound==false){
            throw new Exception("User not found");
        }

        List<User> userList = groupUserMap.get(userGroup);
        //List<User> updatedUserList = new ArrayList<>();

        for(User user1 : userList){
            if(user1.equals(user)){
                userList.remove(user);
                break;
            }
        }
        groupUserMap.put(userGroup, userList);

        List<Message> messageList = groupMessageMap.get(userGroup);
        for(Message message : messageList){
            if(senderMap.get(message).equals(user)){
                messageList.remove(message);
                break;
            }
        }
        groupMessageMap.put(userGroup, messageList);

        for(Message message : senderMap.keySet()){
            if(senderMap.get(message).equals(user)){
                senderMap.remove(user);
                break;
            }
        }

        return groupUserMap.get(userGroup).size() + groupMessageMap.get(userGroup).size() + senderMap.size();
    }
    public String findMessage(Date start, Date end, int K) throws Exception{
        //This is a bonus problem and does not contains any marks
        // Find the Kth latest message between start and end (excluding start and end)
        // If the number of messages between given time is less than K, throw "K is greater than the number of messages" exception
        List<Message> messageList = new ArrayList<>();
        for(Group group : groupUserMap.keySet()){
            messageList.addAll(groupMessageMap.get((group)));
        }

        List<Message> list = new ArrayList<>();
        for(Message message : messageList){
            if(message.getTimestamp().after(start) && message.getTimestamp().before(end)){
                list.add(message);
            }
        }
        if(list.size()>K){
            throw new Exception("K is greater than the number of messages");
        }
        Collections.sort(list, new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                return o2.getTimestamp().compareTo(o1.getTimestamp());
            }
        });
        return list.get(K-1).getContent();
    }

}
