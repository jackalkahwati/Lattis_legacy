//
//  ContactsSearchController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/29/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class ContactsSearchController: UITableViewController {
    var interactor: ContactsInteractorInput!
    
    init(_ interactor: ContactsInteractorInput) {
        self.interactor = interactor
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        interactor.reloadSearch = { [unowned self] in
            self.tableView.reloadData()
        }
        tableView.register(UINib(nibName: "ContactCell", bundle: nil), forCellReuseIdentifier: "single")
        tableView.separatorInset = .init(top: 0, left: .margin, bottom: 0, right: .margin)
    }
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return interactor.searchContacts.count
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "single", for: indexPath) as! ContactCell
        cell.contact = interactor.searchContacts[indexPath.row]
        return cell
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let contact = interactor.searchContacts[indexPath.row]
        interactor.didSelect(contact: contact, isSearch: true)
    }
}
