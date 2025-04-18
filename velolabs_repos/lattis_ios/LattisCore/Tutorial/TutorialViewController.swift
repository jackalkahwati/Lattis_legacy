//
//  TutorialViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 14.01.2021.
//  Copyright © 2021 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

final class TutorialViewController: UIViewController {
    
    fileprivate let contentView = UIView()
    fileprivate let scrollView = UIScrollView()
    fileprivate let skipButton = UIButton(type: .custom)
    fileprivate let pageControl = UIPageControl()
    @objc fileprivate let done: () -> Void
    
    init(_ completion: @escaping () -> Void) {
        self.done = completion
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }
        
        view.backgroundColor = UIColor(white: 0, alpha: 0.5)
        
        view.addSubview(contentView)
        contentView.backgroundColor = .white
        contentView.layer.cornerRadius = .containerCornerRadius
        contentView.addShadow()
        
        contentView.addSubview(skipButton)
        let lineView = UIView.line
        
        contentView.addSubview(lineView)
        
        contentView.addSubview(scrollView)
        scrollView.isPagingEnabled = true
        scrollView.showsVerticalScrollIndicator = false
        scrollView.showsHorizontalScrollIndicator = false
        scrollView.layer.masksToBounds = true
        scrollView.layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
        scrollView.layer.cornerRadius = .containerCornerRadius
        
        contentView.addSubview(pageControl)
        pageControl.isUserInteractionEnabled = false
        pageControl.currentPageIndicatorTintColor = .black
        pageControl.pageIndicatorTintColor = UIColor(white: 0, alpha: 0.4)
        
        constrain(contentView, skipButton, lineView, scrollView, pageControl, view) { content, skip, line, scroll, page, view in
            content.left == view.left + .margin/2
            content.right == view.right - .margin/2
            content.bottom == view.safeAreaLayoutGuide.bottom - .margin/2
            content.top == view.safeAreaLayoutGuide.top + .margin/2
        
            skip.bottom == content.bottom
            skip.left == content.left
            skip.right == content.right
            skip.height == 44
            
            line.bottom == skip.top
            line.left == content.left + .margin/2
            line.right == content.right - .margin/2
            
            page.bottom == line.top
            page.left == content.left
            page.right == content.right
            page.height == 33
            
            scroll.left == content.left
            scroll.right == content.right
            scroll.top == content.top
            scroll.bottom == page.top
        }
        
        skipButton.setTitle("skip_label".localized(), for: .normal)
        skipButton.setTitleColor(.black, for: .normal)
        skipButton.tintColor = .black
        skipButton.addTarget(self, action: #selector(skip), for: .touchUpInside)
        
        var leftEdge: Edge?
        let files = TutorialManager.shared.files
        pageControl.numberOfPages = files.count
        for (idx, path) in files.enumerated() {
            let view = UIImageView(image: UIImage(contentsOfFile: path))
            view.contentMode = .scaleAspectFit
            self.scrollView.addSubview(view)
            constrain(view, self.scrollView, self.view) { image, scroll, view in
                image.top == scroll.top + .margin/2
                image.bottom == scroll.bottom - .margin/2
                image.height == scroll.height - .margin
                image.width == scroll.width - .margin
                if let edge = leftEdge {
                    image.left == edge + .margin
                } else {
                    image.left == scroll.left + .margin/2
                }
                if idx == files.count - 1 {
                    image.right == scroll.right -  .margin
                } else {
                    leftEdge = image.right
                }
            }
        }
        scrollView.delegate = self
    }
    
    @objc
    fileprivate func skip() {
        dismiss(animated: true, completion: done)
    }
}

extension TutorialViewController: UIScrollViewDelegate {
    func scrollViewDidEndScrollingAnimation(_ scrollView: UIScrollView) {
        pageControl.currentPage = scrollView.currentPage
    }
    
    func scrollViewDidEndDecelerating(_ scrollView: UIScrollView) {
        pageControl.currentPage = scrollView.currentPage
    }
}

extension UIScrollView {
    var currentPage: Int{
        return Int(contentOffset.x/bounds.size.width)
    }
}
