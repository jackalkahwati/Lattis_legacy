//
//  SLLog+CoreDataProperties.h
//  Skylock
//
//  Created by Andre Green on 5/14/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//
//  Choose "Create NSManagedObject Subclass…" from the Core Data editor menu
//  to delete and recreate this implementation file for your updated model.
//

#import "SLLog.h"

NS_ASSUME_NONNULL_BEGIN

@interface SLLog (CoreDataProperties)

@property (nullable, nonatomic, retain) NSString *entry;
@property (nullable, nonatomic, retain) NSDate *date;

@end

NS_ASSUME_NONNULL_END
