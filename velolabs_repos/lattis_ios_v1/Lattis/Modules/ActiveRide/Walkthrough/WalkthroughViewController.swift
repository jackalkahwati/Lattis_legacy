//
//  WalkthroughViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 7/17/17.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import Pageboy

class WalkthroughViewController: UIViewController {
    @IBOutlet weak var pageControl: UIPageControl!
    
    fileprivate var controllers: [UIViewController] = []
    fileprivate var pageController: PageboyViewController?

    override func viewDidLoad() {
        super.viewDidLoad()

        let unlock = storyboard!.instantiateViewController(withIdentifier: "unlock")
        let pull = storyboard!.instantiateViewController(withIdentifier: "pull")
        let push = storyboard!.instantiateViewController(withIdentifier: "push")
        let lock = storyboard!.instantiateViewController(withIdentifier: "lock")
        let stow = storyboard!.instantiateViewController(withIdentifier: "stow")
        controllers = [unlock, pull, push, lock, stow]
        
        pageController?.reloadData()
        
    }
    
    @IBAction func skipAction(_ sender: Any) {
        UIView.animate(withDuration: .defaultAnimation, animations: { 
            self.view.alpha = 0
        }, completion: { _ in
            self.view.removeFromSuperview()
            self.removeFromParent()
        })
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if let page = segue.destination as? PageboyViewController {
            page.dataSource = self
            page.delegate = self
            page.reloadData()
            pageController = page
        }
    }
}

extension WalkthroughViewController: PageboyViewControllerDataSource, PageboyViewControllerDelegate {
    func pageboyViewController(_ pageboyViewController: PageboyViewController, willScrollToPageAt index: PageboyViewController.PageIndex, direction: PageboyViewController.NavigationDirection, animated: Bool) {
        
    }
    
    func pageboyViewController(_ pageboyViewController: PageboyViewController, didScrollTo position: CGPoint, direction: PageboyViewController.NavigationDirection, animated: Bool) {
        
    }
    
    func pageboyViewController(_ pageboyViewController: PageboyViewController, didScrollToPageAt index: PageboyViewController.PageIndex, direction: PageboyViewController.NavigationDirection, animated: Bool) {
        
    }
    
    func pageboyViewController(_ pageboyViewController: PageboyViewController, didReloadWith currentViewController: UIViewController, currentPageIndex: PageboyViewController.PageIndex) {
        
    }
    
    func numberOfViewControllers(in pageboyViewController: PageboyViewController) -> Int {
        return controllers.count
    }
    
    func viewController(for pageboyViewController: PageboyViewController, at index: PageboyViewController.PageIndex) -> UIViewController? {
        return controllers[index]
    }
    
    func defaultPage(for pageboyViewController: PageboyViewController) -> PageboyViewController.Page? {
        return nil
    }
}
