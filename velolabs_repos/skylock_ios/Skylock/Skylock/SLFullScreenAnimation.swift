//
//  SLFullScreenAnimation.swift
//  Skylock
//
//  Created by Andre Green on 6/14/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit

enum SLFullScreenAnimationHandlerPhase {
    case Presenting
    case Dismissing
}

class SLFullScreenAnimation:NSObject, UIViewControllerAnimatedTransitioning {
    var phase:SLFullScreenAnimationHandlerPhase
    
    init(phase: SLFullScreenAnimationHandlerPhase) {
        self.phase = phase
        super.init()
    }
    
    func transitionDuration(using transitionContext: UIViewControllerContextTransitioning?) -> TimeInterval {
        return 0.4
    }
    
    func animateTransition(using transitionContext: UIViewControllerContextTransitioning) {
        if let fromController = transitionContext.viewController(forKey: UITransitionContextViewControllerKey.from), let toController = transitionContext.viewController(forKey: UITransitionContextViewControllerKey.to) {
            let endFrame:CGRect
            
            switch self.phase {
            case .Presenting:
                transitionContext.containerView.addSubview(fromController.view)
                transitionContext.containerView.addSubview(toController.view)
                
                toController.view.frame = CGRect(
                    x: fromController.view.bounds.size.width,
                    y: 0.0,
                    width: toController.view.bounds.size.width,
                    height: toController.view.bounds.size.height
                )
                
                endFrame = transitionContext.finalFrame(for: fromController)
                UIView.animate(
                    withDuration: self.transitionDuration(using: transitionContext),
                    animations: {
                        toController.view.frame = endFrame
                    },
                    completion: { (finished) in
                        transitionContext.completeTransition(true)
                    }
                )
            case .Dismissing:
                endFrame = CGRect(
                    x: toController.view.bounds.size.width,
                    y: 0.0,
                    width: fromController.view.bounds.size.width,
                    height: fromController.view.bounds.size.height
                )
                
                UIView.animate(
                    withDuration: self.transitionDuration(using: transitionContext),
                    animations: {
                        fromController.view.frame = endFrame
                    },
                    completion: { (finished) in
                        fromController.view.removeFromSuperview()
                        transitionContext.completeTransition(false)
                    }
                )
            }
        }
    }
}
