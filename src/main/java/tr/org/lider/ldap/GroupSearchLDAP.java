package tr.org.lider.ldap;

import java.util.ArrayList;
import java.util.Iterator;

//
//class GroupLinkedList
//{
//    // head of list
//    Node head; 
// 
//    // Linked list Node
//    class Node
//    {
//        Boolean viseted;
//        String currentDn;
//        Node next;
//
//        Node(String dn, Boolean vis )
//        {
//        	viseted = vis;
//        	currentDn = dn;
//            next = null;
//        }
//    }
// 
//    // Inserts a new Node at front
//    // of the list.
//    public void push(String key, Boolean value)
//    {
//        /* 1 & 2: Allocate the Node &
//                  Put in the data*/
//        Node new_node = new Node(key, value);
// 
//        // 3. Make next of new Node as head
//        new_node.next = head;
// 
//        // 4. Move the head to point to
//        // new Node
//        head = new_node;
//    }
// 
//    // Inserts a new node after the
//    // given prev_node.
//    public void insertAfter(Node prev_node, String key, Boolean value)
//    {
//        // 1. Check if the given Node is null
//        if (prev_node == null)
//        {
//            System.out.println(
//                   "The given previous node cannot be null");
//            return;
//        }
// 
//        /* 2 & 3: Allocate the Node &
//                  Put in the data*/
//        Node new_node = new Node(key, value);
// 
//        // 4. Make next of new Node as next
//        // of prev_node
//        new_node.next = prev_node.next;
// 
//        // 5. make next of prev_node as
//        // new_node
//        prev_node.next = new_node;
//    }
//    
//    public boolean findDn(String dn) {
//        Node current = head;
//        while (current != null) {
//          if (current.currentDn.equals(dn)) {
//            return true;
//          }
//          current = current.next;
//        }
//        return false;
//      }
//    
//    public boolean visited(Boolean visited) {
//        Node current = head;
//        while (current != null) {
//          if (current.currentDn.equals(visited)) {
//            return true;
//          }
//          current = current.next;
//        }
//        return false;
//      }
//    
//    /* Appends a new node at the end.
//       This method is defined inside
//       LinkedList class shown above */
//    
//    public void updateValue(Node head, Boolean val, Boolean newVal) {
//        Node current = head;
//        while (current != null) {
//            if (current.viseted == val) {
//                current.viseted = newVal;
//                break;
//            }
//            current = current.next;
//        }
//    }
//    public void append(String key, Boolean value)
//    {
//        /* 1. Allocate the Node &
//           2. Put in the data
//           3. Set next as null */
//        Node new_node = new Node(key, value);
// 
//        /* 4. If the Linked List is empty,
//              then make the new node as head */
//        if (head == null)
//        {
//            head = new Node(key, value);
//            return;
//        }
// 
//        /* 4. This new node is going to be
//              the last node, so make next
//              of it as null */
//        new_node.next = null;
// 
//        // 5. Else traverse till the last node
//        Node last = head;
//        while (last.next != null)
//            last = last.next;
// 
//        // 6. Change the next of last node
//        last.next = new_node;
//        return;
//    }
// 
//    /* This function prints contents of
//       linked list starting from the
//       given node */
//    public void printList()
//    {
//        Node tnode = head;
//        while (tnode != null)
//        {
//            System.out.println(tnode.currentDn + " ," + tnode.viseted + " ---- ");
//            tnode = tnode.next;
//        }
//    }
// 
//}
public class GroupSearchLDAP {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		LinkedList llist = new LinkedList();
		 
		 System.out.println("DOMATES");
//		 llist.printList();
	}

}
