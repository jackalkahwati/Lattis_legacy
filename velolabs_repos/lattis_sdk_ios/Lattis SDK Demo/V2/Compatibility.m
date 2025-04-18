//
//  Compatibility.m
//  Lattis SDK Demo
//
//  Created by Ravil Khusainov on 20/03/2019.
//  Copyright © 2019 Lattis Inc. All rights reserved.
//

#import "Compatibility.h"

@interface Compatibility()
{
    EllipseManager *manager;
    Ellipse *_lock;
}
@end

@implementation Compatibility



- (void) test {
    manager = [EllipseManager shared];
    [manager scanWith:self];
    
    [_lock connectWithHandler:self];
    [_lock unlock];
}

- (void)manager:(EllipseManager * _Nonnull)lockManager didRestoreConnected:(NSArray<Ellipse *> * _Nonnull)locks {
    
}

- (void)manager:(EllipseManager * _Nonnull)lockManager didUpdateConnectionState:(BOOL)connected {
    
}

- (void)manager:(EllipseManager * _Nonnull)lockManager didUpdateLocks:(NSArray<Ellipse *> * _Nonnull)insert delete:(NSArray<Ellipse *> * _Nonnull)delete_ {
    
}

- (void)ellipse:(Ellipse * _Nonnull)ellipse didUpdate:(enum LSEllipseSecurity)security {
    
}

- (void)ellipse:(Ellipse * _Nonnull)ellipse didUpdate:(enum LSEllipseConnection)connection error:(NSError * _Nullable)error {
    
}

- (void)ellipse:(Ellipse * _Nonnull)ellipse didUpdate:(id _Nonnull)value with:(enum LSEllipseValue)valueType {
    if(valueType == LSEllipseValueMagnetAutoLockEnabled) {
        NSNumber *num = value;
        BOOL isEnabled = [num boolValue];
        if(!isEnabled) {
            [ellipse setObjcIsMagnetAutoLockEnabled:YES];
        }
    }
}

@end
