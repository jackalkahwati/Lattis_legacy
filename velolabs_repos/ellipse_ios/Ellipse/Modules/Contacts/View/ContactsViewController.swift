//
//  ContactsContactsViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 08/11/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class ContactsViewController: ViewController {
    @IBOutlet weak var bottomLayout: NSLayoutConstraint!
    @IBOutlet weak var hintLabel: UILabel!
    @IBOutlet weak var tableView: UITableView!
    var interactor: ContactsInteractorInput!
    var searchController: UISearchController!

    override func viewDidLoad() {
        super.viewDidLoad()

        addCloseButton()
        
        searchController = UISearchController(searchResultsController: ContactsSearchController(interactor))
        searchController.searchResultsUpdater = self
        searchController.searchBar.tintColor = .elSteel
        searchController.searchBar.backgroundColor = .white
        
        navigationItem.searchController = searchController
        navigationItem.hidesSearchBarWhenScrolling = false
        UITextField.appearance(whenContainedInInstancesOf: [UISearchBar.self]).tintColor = .elSteel
        UITextField.appearance(whenContainedInInstancesOf: [UISearchBar.self]).defaultTextAttributes = [.foregroundColor: UIColor.black]
        UIBarButtonItem.appearance(whenContainedInInstancesOf: [UISearchBar.self]).setTitleTextAttributes([.font: UIFont.elButtonSmall], for: .normal)
        searchController.searchBar.clipsToBounds = true
        
        if case .multiple = interactor.selection {
            navigationItem.rightBarButtonItem = UIBarButtonItem(title: "profile_save".localized(), style: .plain, target: self, action: #selector(save))
            bottomLayout.priority = .defaultLow
        }
        
        tableView.tableFooterView = UIView()
        tableView.tintColor = .elDarkSkyBlue
        tableView.register(ContactSectionHeader.self, forHeaderFooterViewReuseIdentifier: "header")
        tableView.delegate = self
        tableView.dataSource = self
        
        interactor.start()
    }
    
    @objc func save() {
        interactor.saveSelected()
    }
}

extension ContactsViewController: UITableViewDataSource, UITableViewDelegate {
    func numberOfSections(in tableView: UITableView) -> Int {
        return interactor.numberOfSections
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return interactor.numberOfRows(in: section)
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let contact = interactor.item(for: indexPath)
        let cell = tableView.dequeueReusableCell(withIdentifier: interactor.selection.identifire, for: indexPath) as! ContactCell
        cell.contact = contact
        cell.isChecked = interactor.isSelected(contact: contact)
        return cell
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let view = tableView.dequeueReusableHeaderFooterView(withIdentifier: "header") as? ContactSectionHeader
        view?.titleLabel.text = interactor.title(for: section)
        return view
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        interactor.didSelect(contact: interactor.item(for: indexPath), isSearch: false)
        reloadRows(at: [indexPath])
    }
    
    func sectionIndexTitles(for tableView: UITableView) -> [String]? {
        return "ABCDEFGHIJKLMNOPQRSTUVWXYZ".compactMap{String($0)}
    }
    
    func tableView(_ tableView: UITableView, sectionForSectionIndexTitle title: String, at index: Int) -> Int {
        return interactor.sectionTitles.filter{$0 <= title}.count - 1
    }
}

extension ContactsViewController: ContactsInteractorOutput {
    func refresh() {
        tableView.reloadData()
        searchController.isActive = false
    }
    
    func reloadRows(at indexPaths: [IndexPath]) {
        tableView.beginUpdates()
        tableView.reloadRows(at: indexPaths, with: .automatic)
        tableView.endUpdates()
    }
    
    func reloadHint(left: Int) {
        hintLabel.text = left > 0 ? "contacts_limit_hint".localizedFormat("\(left)") : "contacts_limit_hint_full".localized()
    }
    
    func remindLimit() {
        UIView.animate(withDuration: 0.35, delay: 0, options: .curveEaseIn, animations: {
            self.hintLabel.superview?.backgroundColor = .red
        }, completion: { _ in
            UIView.animate(withDuration: 0.35, delay: 0, options: .curveEaseOut, animations: {
                self.hintLabel.superview?.backgroundColor = .elWhite
            }, completion: nil)
        })
    }
}

extension ContactsViewController: UISearchResultsUpdating {
    func updateSearchResults(for searchController: UISearchController) {
        interactor.search(searchController.searchBar.text!)
    }
}

extension ContactsViewController: UISearchControllerDelegate {
    func willPresentSearchController(_ searchController: UISearchController) {
        searchController.searchResultsController?.view.isHidden = false
        navigationController?.navigationBar.isTranslucent = true
    }
    
    func didDismissSearchController(_ searchController: UISearchController) {
        navigationController?.navigationBar.isTranslucent = false
    }
}

// Helper function inserted by Swift 4.2 migrator.
fileprivate func convertToNSAttributedStringKeyDictionary(_ input: [String: Any]) -> [NSAttributedString.Key: Any] {
	return Dictionary(uniqueKeysWithValues: input.map { key, value in (NSAttributedString.Key(rawValue: key), value)})
}
