//
//  SLDatabaseManager.h
//  Skylock
//
//  Created by Andre Green on 7/5/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

#import <Foundation/Foundation.h>

@class SLUser;
@class SLLock;
@class SLEmergencyContact;
@class NSManagedObjectContext;

@interface SLDatabaseManager : NSObject


+(instancetype _Nonnull)shared;

@property (nonatomic, strong) NSManagedObjectContext * _Nonnull context;
    
- (SLUser * _Nullable)getCurrentUser;
- (SLUser * _Nullable)ownerOfLock:(int32_t)lockId;
- (SLUser * _Nullable)ownerOfLockWithMacId:(NSString * _Nonnull)macId;
- (void)saveLockToDb:(SLLock * _Nonnull)lock withCompletion:(void(^ _Nullable)(BOOL success))completion;
- (NSArray * _Nullable)allLocks;
- (NSArray * _Nullable)locksForCurrentUser;
- (void)deleteLock:(SLLock * _Nonnull)lock withCompletion:(void(^ _Nullable)(BOOL success))completion;
- (void)saveUser:(SLUser * _Nonnull)user withCompletion:(void(^ _Nullable)(BOOL success))completion;
- (void)saveUserWithDictionary:(NSDictionary * _Nonnull)dictionary isFacebookUser:(BOOL)isFacebookUser;
- (void)setCurrentUser;
- (SLLock * _Nullable)getCurrentLockForCurrentUser;
- (void)setCurrentLock:(SLLock * _Nonnull)lock;
- (void)deselectAllLocks;
- (SLLock * _Nullable)getLockWithLockId:(int32_t)lockId;
- (SLLock * _Nullable)getLockWithMacId:(NSString * _Nonnull)macId;
- (SLLock * _Nonnull)newLockWithName:(NSString * _Nonnull)name andUUID:(NSString * _Nonnull)uuid;
- (SLLock * _Nonnull)newLockWithGivenName:(NSString * _Nullable)givenName andMacAddress:(NSString * _Nonnull)macAddress;
- (SLUser * _Nonnull)userWithId:(int32_t)userId usersId:(NSString * _Nullable)usersId;
- (BOOL)doesCurrentUserHaveLock:(SLLock * _Nonnull)lock;
- (NSArray * _Nonnull)getAllLogs;
- (void)saveLogEntry:(NSString * _Nonnull)entry;
- (void)saveLockConnectedDate:(SLLock * _Nonnull)lock;
- (void)saveLock:(SLLock * _Nullable)lock;
- (NSArray <SLEmergencyContact *> * _Nonnull)emergencyContacts;
- (NSArray <SLEmergencyContact *> * _Nullable)emergencyContactsForCurrentUser;
- (void)saveEmergencyContact:(SLEmergencyContact * _Nonnull)contact;
- (SLEmergencyContact * _Nullable)getContactWithContactId:(NSString * _Nonnull)contactId;
- (SLEmergencyContact * _Nonnull)newEmergencyContact;
- (void)deleteContactWithId:(NSString * _Nonnull)contactId completion:(void(^ _Nullable)(BOOL success))completion;
- (NSError * _Nullable)deleteEmergencyContact:(SLEmergencyContact * _Nonnull)contact;
- (void)deleteUser:(SLUser * _Nonnull)user;

@end
