//
//  SLDirectionsTableViewCell.m
//  Skylock
//
//  Created by Andre Green on 9/11/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

#import "SLDirectionsTableViewCell.h"
#import "Ellipse-Swift.h"
#import "NSString+Skylock.h"    


@interface SLDirectionsTableViewCell()


@end

@implementation SLDirectionsTableViewCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:reuseIdentifier];
    if (self) {
        self.textLabel.font = [UIFont fontWithName:@"OpenSans-Bold" size:14];
        self.textLabel.textColor = [UIColor color:84 green:164 blue:212];
        
        self.detailTextLabel.font = [UIFont fontWithName:@"OpenSans" size:12];
        self.detailTextLabel.textColor = [UIColor color:155 green:155 blue:155];
        self.detailTextLabel.numberOfLines = 2;
        
        self.backgroundColor = [UIColor clearColor];
        self.contentView.backgroundColor = [UIColor clearColor];
    }
    
    return self;
}

- (void)setPropertiesWithDirection:(NSDictionary *)properties isFirstDirection:(BOOL)isFirst isLastDirection:(BOOL)isLast
{
    if (!properties[@"top"] || !properties[@"bottom"]) {
        return;
    }
    
    self.textLabel.text = properties[@"top"];
    self.detailTextLabel.text = properties[@"bottom"];
    
    if (isFirst) {
        self.imageView.image = [UIImage imageNamed:@"map_directions_start_icon"];
    } else if (isLast) {
        self.imageView.image = [UIImage imageNamed:@"map_currently_connected_bike_icon_small"];
    }
}

- (void)prepareForReuse
{
    [super prepareForReuse];
    
    self.imageView.image = nil;
    self.textLabel.text = nil;
    self.detailTextLabel.text = nil;
}

@end
