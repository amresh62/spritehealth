// package com.spritehealth.services.impl;

// import com.spritehealth.models.User;
// import com.spritehealth.services.interfaces.IUserDatastoreService;

// import java.util.*;
// import java.util.concurrent.ConcurrentHashMap;
// import java.util.concurrent.atomic.AtomicLong;
// import java.util.stream.Collectors;

// /**
//  * In-memory implementation of user storage for local testing
//  * This does NOT persist data - all data is lost when the server restarts
//  */
// public class InMemoryDatastoreServiceImpl implements IUserDatastoreService {
//     private static final Map<Long, User> storage = new ConcurrentHashMap<>();
//     private static final AtomicLong idGenerator = new AtomicLong(1);

//     @Override
//     public User createUser(User user) {
//         long id = idGenerator.getAndIncrement();
//         User newUser = new User(
//                 user.getName(),
//                 user.getDateOfBirth(),
//                 user.getEmail(),
//                 user.getPassword(),
//                 user.getPhone(),
//                 user.getGender(),
//                 user.getAddress()
//         );
//         newUser.setId(id);
//         storage.put(id, newUser);
//         System.out.println("Created user: " + newUser.getName() + " (ID: " + id + ")");
//         return newUser;
//     }

//     @Override
//     public List<User> createUsers(List<User> users) {
//         List<User> createdUsers = new ArrayList<>();
//         for (User user : users) {
//             createdUsers.add(createUser(user));
//         }
//         System.out.println("Created " + createdUsers.size() + " users");
//         return createdUsers;
//     }

//     @Override
//     public User getUserById(Long id) {
//         return storage.get(id);
//     }

//     @Override
//     public User getUserByEmail(String email) {
//         return storage.values().stream()
//                 .filter(user -> user.getEmail().equalsIgnoreCase(email))
//                 .findFirst()
//                 .orElse(null);
//     }

//     @Override
//     public List<User> getAllUsers() {
//         return new ArrayList<>(storage.values());
//     }

//     @Override
//     public List<User> queryUsersByName(String name) {
//         return storage.values().stream()
//                 .filter(user -> user.getName().toLowerCase().contains(name.toLowerCase()))
//                 .collect(Collectors.toList());
//     }

//     @Override
//     public User updateUser(User user) {
//         if (user.getId() != null) {
//             long id = user.getId();
//             storage.put(id, user);
//             return user;
//         }
//         return null;
//     }

//     @Override
//     public boolean deleteUser(String id) {
//         try {
//             long longId = Long.parseLong(id);
//             User removed = storage.remove(longId);
//             if (removed != null) {
//                 System.out.println("Deleted user: " + removed.getName() + " (ID: " + id + ")");
//                 return true;
//             }
//             return false;
//         } catch (NumberFormatException e) {
//             return false;
//         }
//     }

//     @Override
//     public User authenticateUser(String email, String password) {
//         return storage.values().stream()
//                 .filter(user -> user.getEmail().equalsIgnoreCase(email) 
//                         && user.getPassword().equals(password))
//                 .findFirst()
//                 .orElse(null);
//     }

//     // Additional utility methods
//     public int getTotalCount() {
//         return storage.size();
//     }

//     public void clearAll() {
//         storage.clear();
//         System.out.println("Cleared all users from in-memory storage");
//     }
// }
