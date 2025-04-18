//
//  MembershipSearchViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 12.11.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import Model

class MembershipSearchViewController: UIViewController {
    
    fileprivate let tableView = UITableView()
    fileprivate let textField = UITextField()
    fileprivate let closeButton = UIButton(type: .custom)
    fileprivate let memberships: [Membership]
    fileprivate var searchResults: [Membership]
    fileprivate let handler: (Membership) -> Void
    
    init(_ memberships: [Membership], completion: @escaping (Membership) -> Void) {
        self.memberships = memberships
        self.searchResults = memberships
        self.handler = completion
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
        
        view.backgroundColor = .white
        
        let searchIcon = UIImageView(image: .named("icon_search"))
        searchIcon.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        
        view.addSubview(textField)
        view.addSubview(tableView)
        view.addSubview(searchIcon)
        
        textField.placeholder = "search_fleet".localized()
        
        closeButton.setImage(.named("icon_close"), for: .normal)
        closeButton.tintColor = .black
        textField.rightView = closeButton
        textField.rightViewMode = .always
        closeButton.addTarget(self, action: #selector(close), for: .touchUpInside)
        textField.addTarget(self, action: #selector(search), for: .editingChanged)
        
        tableView.register(MembershipCell.self, forCellReuseIdentifier: "cell")
        tableView.estimatedRowHeight = 66
        tableView.rowHeight = UITableView.automaticDimension
        tableView.dataSource = self
        tableView.delegate = self
        tableView.tableFooterView = UIView()
        
        constrain(searchIcon, textField, tableView, view) { icon, search, table, view in
            icon.left == view.left + .margin
            icon.centerY == search.centerY
            
            search.left == icon.right + .margin/2
            search.right == view.right - .margin
            search.top == view.top + .margin
            search.height == 44
            
            table.top == search.bottom + .margin/2
            table.left == view.left
            table.right == view.right
            table.bottom == view.safeAreaLayoutGuide.bottom
        }
        textField.becomeFirstResponder()
    }
    
    @objc
    fileprivate func search() {
        guard let term = textField.text, !term.isEmpty else {
            searchResults = memberships
            tableView.reloadData()
            return
        }
        searchResults = memberships.filter({ mem in
            if let name = mem.fleet.name, name.lowercased().contains(term.lowercased()) {
                return true
            }
            if let city = mem.fleet.address?.city, city.lowercased().contains(term.lowercased()) {
                return true
            }
            return false
        })
        tableView.reloadData()
    }
}

extension MembershipSearchViewController: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        searchResults.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! MembershipCell
        cell.membership = searchResults[indexPath.row]
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        handler(searchResults[indexPath.row])
        close()
    }
}
