//
//  SLPicManager.m
//  Skylock
//
//  Created by Andre Green on 7/19/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

#import "SLPicManager.h"
#import "NSString+Skylock.h"
#import "SLUserDefaults.h"
#import "Ellipse-Swift.h"

#define kSLProfilePicDirPath            @"profie_pics"
#define kSLProfilePicCacheRefreshDays   2

@interface SLPicManager()

@property (strong) NSCache *profilePicCache;
@property (nonatomic, strong) NSString *documentDirPath;

@end

@implementation SLPicManager

- (id)init
{
    self = [super init];
    if (self) {
        _profilePicCache = [[NSCache alloc] init];
        _profilePicCache.countLimit = 20;
        [self createProfilePicDirectory];
        [self refreshProfilePicCache];
    }
    
    return self;
}

+ (instancetype)shared
{
    NSLog(@"%@ %@", NSStringFromClass([self class]), NSStringFromSelector(_cmd));
    static SLPicManager *picManager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        picManager = [[self alloc] init];
    });
    
    return picManager;
}


- (NSString *)documentDirPath
{
    if (!_documentDirPath) {
        _documentDirPath = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES)[0];
    }
    
    return _documentDirPath;
}

- (NSString *)profilePicPathForFileWithHashedName:(NSString *)hashedName
{
    return [NSString pathWithComponents:@[self.documentDirPath,
                                          kSLProfilePicDirPath,
                                          hashedName]];
}

- (BOOL)createProfilePicDirectory
{
    NSFileManager *fm = [NSFileManager defaultManager];
    NSString *dirPath = [NSString pathWithComponents:@[self.documentDirPath, kSLProfilePicDirPath]];
    if (![fm fileExistsAtPath:dirPath]) {
        NSError *error;
        [fm createDirectoryAtPath:dirPath withIntermediateDirectories:YES attributes:nil error:&error];
        if (error) {
            NSLog(@"Could not create profile pic directory at path: %@", dirPath);
            return NO;
        }
    }
    
    return YES;
}

- (void)getPicWithUserId:(NSString *)userId withCompletion:(void (^)(UIImage *))completion
{
    NSString *hash = [userId MD5String];
    if ([self.profilePicCache objectForKey:hash]) {
        if (completion) completion([self.profilePicCache objectForKey:hash]);
        return;
    }

    NSString *path = [self profilePicPathForFileWithHashedName:hash];
    
    if ([[NSFileManager defaultManager] fileExistsAtPath:path]) {
        NSData *data = [NSData dataWithContentsOfFile:path];
        UIImage *pic = [UIImage imageWithData:data];
        
        [self.profilePicCache setObject:pic forKey:hash];
        if (completion) completion(pic);
        return;
    }
    
    if (completion) completion(nil);
}

- (void)savePicture:(UIImage *)image forUserId:(NSString *)userId
{
    NSString *hash = [userId MD5String];
    NSString *path = [self profilePicPathForFileWithHashedName:hash];
    NSData *data = [NSData dataWithData:UIImageJPEGRepresentation(image, 1.0f)];
    if (![data writeToFile:path atomically:YES]) {
        NSLog(@"Error writing file to path: %@", path);
    }
    
    [self.profilePicCache setObject:image forKey:hash];
}

- (BOOL)shouldRefreshProfilePicCache
{
    NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
    if ([ud objectForKey:SLUserDefaultsProfilePicCacheDate]) {
        NSCalendar *calendar = [[NSCalendar alloc] initWithCalendarIdentifier:NSCalendarIdentifierGregorian];
        NSDate *lastRefreshDate = [ud objectForKey:SLUserDefaultsProfilePicCacheDate];
        NSDate *currentDate = [NSDate date];
        NSDateComponents *differenceComps = [calendar components:NSCalendarUnitDay
                                                        fromDate:lastRefreshDate
                                                          toDate:currentDate
                                                         options:0];
        
        return differenceComps.day >= kSLProfilePicCacheRefreshDays;
    }
    
    [ud setObject:[NSDate date] forKey:SLUserDefaultsProfilePicCacheDate];
    [ud synchronize];
    
    return NO;
}

- (void)refreshProfilePicCache
{
    if (self.shouldRefreshProfilePicCache) {
        NSString *directoryPath = [NSString pathWithComponents:@[self.documentDirPath, kSLProfilePicDirPath]];
        NSFileManager *fm = [NSFileManager defaultManager];
        if ([fm fileExistsAtPath:directoryPath]) {
            NSError *error;
            NSArray *files = [fm contentsOfDirectoryAtPath:directoryPath error:&error];
            if (error) {
                NSLog(@"Error getting files from directory: %@", directoryPath);
                return;
            }
            
            for (NSString *fileName in files) {
                NSString *filePath = [NSString pathWithComponents:@[directoryPath, fileName]];
                [fm removeItemAtPath:filePath error:&error];
                
                if (error) {
                    NSLog(@"Error removing file at path: %@", filePath);
                    error = nil;
                }
            }
            
            NSUserDefaults *ud = [NSUserDefaults standardUserDefaults];
            [ud setObject:[NSDate date] forKey:SLUserDefaultsProfilePicCacheDate];
            [ud synchronize];
        }
    }
}

- (void)facebookPicForFBUserId:(NSString *)fbUserId completion:(void (^)(UIImage *))completion
{
    [self getPicWithUserId:fbUserId withCompletion:^(UIImage *cacheImage) {
        if (cacheImage) {
            if (completion) completion(cacheImage);
            return;
        }

        [[FacebookService shared] getFacebookPicWithUserId:fbUserId completion:^(UIImage *image) {
            if (image) {
                [self savePicture:image forUserId:fbUserId];
                if (completion) {
                    completion(image);
                }
                return;
            }
            
            if (completion) completion(nil);
        }];
    }];
}

- (UIImage *)userImageForUserId:(NSString *)userId
{
    NSString *hash = [userId MD5String];
    if ([self.profilePicCache objectForKey:hash]) {
        return [self.profilePicCache objectForKey:hash];
    }
    
    NSString *path = [self profilePicPathForFileWithHashedName:hash];
    
    if ([[NSFileManager defaultManager] fileExistsAtPath:path]) {
        NSData *data = [NSData dataWithContentsOfFile:path];
        UIImage *pic = [UIImage imageWithData:data];
        
        [self.profilePicCache setObject:pic forKey:hash];
        return pic;
    }
    
    return nil;
}

@end
