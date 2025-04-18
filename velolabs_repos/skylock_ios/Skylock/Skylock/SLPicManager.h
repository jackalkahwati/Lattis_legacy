//
//  SLPicManager.h
//  Skylock
//
//  Created by Andre Green on 7/19/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@interface SLPicManager : NSObject

+ (instancetype _Nonnull)shared;

- (void)getPicWithUserId:(NSString * _Nonnull)userId withCompletion:(void(^ _Nullable)(UIImage * _Nullable))completion;
- (void)refreshProfilePicCache;
- (void)facebookPicForFBUserId:(NSString * _Nonnull)fbUserId
                    completion:(void(^ _Nullable)(UIImage * _Nullable))completion;
- (UIImage * _Nullable)userImageForUserId:(NSString * _Nonnull)userId;
- (void)savePicture:(UIImage * _Nonnull)image forUserId:(NSString * _Nonnull)userId;
@end
