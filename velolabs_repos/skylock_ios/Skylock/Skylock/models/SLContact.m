//
//  SLContact.m
//  Skylock
//
//  Created by Andre Green on 6/27/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

#import "SLContact.h"

@implementation SLContact

- (id)initWithFirstName:(NSString *)firstName
               lastName:(NSString *)lastName
                  email:(NSString *)email
            phoneNumber:(NSString *)phoneNumber
              imageData:(NSData *)imageData
{
    self = [super init];
    
    if (self) {
        _firstName = firstName;
        _lastName = lastName;
        _email = email;
        _phoneNumber = phoneNumber;
        _imageData = imageData;
        _hasBeenShared = NO;
        _fullName = self.makeFullName;
    }
    
    return self;
}

+ (id)contactWithFirstName:(NSString *)firstName
                  lastName:(NSString *)lastName
                     email:(NSString *)email
               phoneNumber:(NSString *)phoneNumber
                 imageData:(NSData *)imageData
{
    return [[self alloc] initWithFirstName:firstName
                                  lastName:lastName
                                     email:email
                               phoneNumber:phoneNumber
                                 imageData:imageData];
}

- (NSString *)makeFullName
{
    NSMutableString *name = [NSMutableString new];
    if (self.firstName) {
        [name appendString:self.firstName];
    }
    
    if (self.lastName) {
        NSString *lastName = self.firstName ? [NSString stringWithFormat:@" %@", self.lastName]:self.lastName;
        [name appendString:lastName];
    }
    
    return name;
}
@end
