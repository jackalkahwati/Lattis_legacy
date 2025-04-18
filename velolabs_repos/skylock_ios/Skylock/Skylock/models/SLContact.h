//
//  SLContact.h
//  Skylock
//
//  Created by Andre Green on 6/27/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface SLContact : NSObject

@property (nonatomic, copy) NSString *firstName;
@property (nonatomic, copy) NSString *lastName;
@property (nonatomic, copy) NSString *email;
@property (nonatomic, copy) NSString *phoneNumber;
@property (nonatomic, strong) NSData *imageData;
@property (nonatomic, assign) BOOL hasBeenShared;
@property (nonatomic, copy) NSString *fullName;

- (id)initWithFirstName:(NSString *)firstName lastName:(NSString *)lastName email:(NSString *)email phoneNumber:(NSString *)phoneNumber imageData:(NSData *)imageData;

+ (id)contactWithFirstName:(NSString *)firstName lastName:(NSString *)lastName email:(NSString *)email phoneNumber:(NSString *)phoneNumber imageData:(NSData *)imageData;

@end
