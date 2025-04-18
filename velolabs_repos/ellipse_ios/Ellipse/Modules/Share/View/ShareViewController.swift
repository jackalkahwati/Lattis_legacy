//
//  ShareShareViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 07/11/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Cartography

class ShareViewController: ViewController {
    
    var interactor: ShareInteractorInput!
    fileprivate let emptyView = UIView()
    fileprivate let tableView = UITableView()
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .default
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        title = "sharing".localized()
        addMenuButton()
        
        view.addSubview(emptyView)
        view.addSubview(tableView)
        
        tableView.tableFooterView = UIView()
        tableView.separatorInset = .init(top: 0, left: .margin, bottom: 0, right: .margin)
        tableView.separatorColor = .elWarmGrey
        tableView.rowHeight = 120
        tableView.register(SharingLockCell.self, forCellReuseIdentifier: "ellipse")
        
        constrain(emptyView, tableView, view) { empty, table, view in
            empty.edges == view.edges
            table.edges == view.edges
        }
        
        configureEmptyView()
        
        tableView.delegate = self
        tableView.dataSource = self
        
        interactor.start()
    }
    
    fileprivate func configureEmptyView() {
        emptyView.backgroundColor = .white
        let containerView = UIView()
        let titleLabel = UILabel()
        let addButton = UIButton(type: .custom)
        
        emptyView.addSubview(containerView)
        containerView.addSubview(titleLabel)
        containerView.addSubview(addButton)
        emptyView.isHidden = true
        
        smallPositiveStyle(addButton)
        addButton.setTitle("add_new".localized().lowercased().capitalized, for: .normal)
        titleLabel.numberOfLines = 2
        titleLabel.font = .elRegular
        titleLabel.textColor = .elBrownGreyThree
        titleLabel.textAlignment = .center
        titleLabel.text = "sharing_nolocks_found".localized()
        
        constrain(emptyView, containerView, titleLabel, addButton) { empty, container, title, add in
            container.centerY == empty.centerY
            container.left == empty.left + .margin*3
            container.right == empty.right - .margin*3
            
            title.top == container.top
            add.bottom == container.safeAreaLayoutGuide.bottom
            title.left == container.left
            title.right == container.right
            title.bottom == add.top - .margin
            add.centerX == container.centerX
        }
        
        addButton.addTarget(self, action: #selector(addNew(_:)), for: .touchUpInside)
    }
    
    @objc fileprivate func addNew(_ sender: Any) {
        interactor.addNew()
    }
    
    @objc fileprivate func hideHint(_ sender: UIButton) {
        title = "sharing".localized()
        UIView.animate(withDuration: 0.35, animations: {
            sender.superview?.alpha = 0
        }, completion: { _ in
            sender.superview?.removeFromSuperview()
        })
    }
}

extension ShareViewController: ShareInteractorOutput {
    func refresh() {
        tableView.reloadData()
    }
    
    func setEmpty(hidden: Bool) {
        emptyView.isHidden = hidden
        if hidden {
            view.insertSubview(tableView, aboveSubview: emptyView)
        } else {
            view.insertSubview(emptyView, aboveSubview: tableView)
        }
    }
    
    func showHint() {
        title = "title_information".localized()
        let hintView = UIView()
        let titleTop = UILabel()
        let bodyTop = UILabel()
        let titleBottom = UILabel()
        let bodyBottom = UILabel()
        let okButton = UIButton(type: .custom)
        view.addSubview(hintView)
        hintView.addSubview(titleTop)
        hintView.addSubview(bodyTop)
        hintView.addSubview(titleBottom)
        hintView.addSubview(bodyBottom)
        hintView.addSubview(okButton)
        
        func titleStyle(_ label: UILabel) {
            label.textColor = .black
            label.font = .elTitle
            label.numberOfLines = 0
        }
        
        func bodyStyle(_ label: UILabel) {
            label.textColor = .elBrownGreyThree
            label.font = .elRegular
            label.numberOfLines = 0
        }
        
        titleStyle(titleTop)
        titleStyle(titleBottom)
        bodyStyle(bodyTop)
        bodyStyle(bodyBottom)
        smallPositiveStyle(okButton)
        okButton.setTitle("ok".localized().lowercased().capitalized, for: .normal)
        titleTop.text = "sharing_hint_title1".localized().lowercased().capitalized
        bodyTop.text = "sharing_hint_text1".localized()
        titleBottom.text = "sharing_hint_title2".localized().lowercased().capitalized
        bodyBottom.text = "sharing_hint_text2".localized()
        
        hintView.backgroundColor = .white
        constrain(hintView, titleTop, titleBottom, bodyTop, bodyBottom, okButton, view) { hint, titleT, titleB, bodyT, bodyB, ok, view in
            hint.edges == view.edges
            ok.bottom == hint.bottom - .margin
            titleT.top == hint.top + .margin
            titleT.left == hint.left + .margin
            titleT.right == hint.right - .margin
            
            bodyT.top == titleT.bottom + .margin
            bodyT.left == titleT.left
            bodyT.right == titleT.right
            
            titleB.top == bodyT.bottom + .margin
            titleB.left == titleT.left
            titleB.right == titleT.right
        
            bodyB.top == titleB.bottom + .margin
            bodyB.left == titleT.left
            bodyB.right == titleT.right
            
            ok.top >= bodyB.bottom + .margin
            ok.left == hint.left + .margin*3
            ok.right == hint.right - .margin*3
        }
        
        okButton.addTarget(self, action: #selector(hideHint(_:)), for: .touchUpInside)
    }
}

extension ShareViewController: UITableViewDataSource, UITableViewDelegate {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return interactor.numberOfRows(in: section)
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let item = interactor.item(for: indexPath)
        let cell = tableView.dequeueReusableCell(withIdentifier: "ellipse") as! SharingLockCell
        cell.delegate = interactor
        cell.ellipse = item
        return cell
    }
}

