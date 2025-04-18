//
//  File.swift
//  Skylock
//
//  Created by Andre Green on 6/14/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit



class SLViewControllerTransitionHandler:NSObject, UIViewControllerTransitioningDelegate {
    func animationControllerForPresentedController(
        presented: UIViewController,
        presentingController presenting: UIViewController,
        sourceController source: UIViewController) -> UIViewControllerAnimatedTransitioning?
    {
        let fullScreenAnimator = SLFullScreenAnimation(phase: .Presenting)
        return fullScreenAnimator;
    }
    
    func animationControllerForDismissedController(dismissed: UIViewController) -> UIViewControllerAnimatedTransitioning? {
        let fullScreenAnimator = SLFullScreenAnimation(phase: .Dismissing)
        return fullScreenAnimator;
    }
}


