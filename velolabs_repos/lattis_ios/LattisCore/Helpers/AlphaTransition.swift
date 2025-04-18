//
//  AnimatedTransition.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 15/05/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit

public class AlphaTransition: NSObject, UIViewControllerAnimatedTransitioning {
    fileprivate let duration: TimeInterval = 0.3
    
    public func transitionDuration(using transitionContext: UIViewControllerContextTransitioning?) -> TimeInterval {
        return duration
    }
    
    public func animateTransition(using transitionContext: UIViewControllerContextTransitioning) {
        guard let fromView = transitionContext.view(forKey: .from) else { return }
        guard let toView = transitionContext.view(forKey: .to) else { return }
        let container = transitionContext.containerView
        container.insertSubview(toView, belowSubview: fromView)
        toView.alpha = 0
        UIView.animate(withDuration: duration, animations: {
            fromView.alpha = 0
            toView.alpha = 1
        }, completion: { (finished) in
            transitionContext.completeTransition(!transitionContext.transitionWasCancelled)
        })
    }
}
