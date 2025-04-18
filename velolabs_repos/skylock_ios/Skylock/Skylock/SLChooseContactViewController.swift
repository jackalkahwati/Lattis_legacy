//
//  SLContactViewController.swift
//  Skylock
//
//  Created by Andre Green on 4/5/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit
import Contacts

protocol SLChooseContactViewControllerDelegate:class {
    func contactViewControllerContactSelected(
        cvc: SLChooseContactViewController,
        contact: SLEmergencyContact,
        isSelected: Bool
    )
    func contactViewControllerWantsExit(cvc: SLChooseContactViewController)
}

class SLChooseContactViewController:
UIViewController,
UITableViewDataSource,
UITableViewDelegate,
SLContactsLetterViewControllerDelegate,
UISearchBarDelegate
{
    let letters:[String] = [
        "a",
        "b",
        "c",
        "d",
        "e",
        "f",
        "g",
        "h",
        "i",
        "j",
        "k",
        "l",
        "m",
        "n",
        "o",
        "p",
        "q",
        "r",
        "s",
        "t",
        "u",
        "v",
        "w",
        "x",
        "y",
        "z"
    ]
    
    var contacts:[String:[CNContact]] = [String:[CNContact]]()
    
    var displayedContacts:[String:[CNContact]] = [String:[CNContact]]()
    
    var currentText: String = ""
    
    let contactCellId:String = String(describing: SLAddContactTableViewCell.self)
    
    var shouldShowNavController: Bool?
    
    weak var delegate: SLChooseContactViewControllerDelegate?
    
    var tableYOffset: CGFloat = 0.0
    
    var searchBarPlaceholderText: String?
    
    var cornerRadius: CGFloat?
    
    let contactHandler = SLContactHandler()
    
    var emergencyContacts = [String:SLEmergencyContact]()
    
    lazy var tableView: UITableView = {
        let frame = CGRect(
            x: 0,
            y: self.searchBar.frame.maxY,
            width: self.view.bounds.size.width,
            height: self.view.bounds.size.height - self.searchBar.bounds.size.height
        )
        
        let table:UITableView = UITableView.init(frame: frame, style: .grouped)
        table.dataSource = self;
        table.delegate = self;
        table.rowHeight = 40.0
        table.separatorInset = UIEdgeInsets.zero
        table.register(SLAddContactTableViewCell.self, forCellReuseIdentifier: self.contactCellId)

        return table
    }()
    
    lazy var letterViewController:SLContactsLetterViewController = {
        let view:SLContactsLetterViewController = SLContactsLetterViewController()
        view.delegate = self
        
        return view
    }()
    
    lazy var searchBar:UISearchBar = {
        let frame = CGRect(
            x: 0.0,
            y: UIApplication.shared.statusBarFrame.size.height
                + self.navigationController!.navigationBar.bounds.size.height,
            width: self.view.bounds.size.width,
            height: 50.0
        )
        
        let bar:UISearchBar = UISearchBar(frame: frame)
        bar.delegate = self
        bar.placeholder = NSLocalizedString("Search", comment: "")
        
        return bar
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.backgroundColor = UIColor.white
        self.navigationItem.title = NSLocalizedString("FIND EMERGENCY CONTACTS", comment: "")
        
        self.getContacts()
        
        if let emergencyContacts = self.contactHandler.getActiveEmergencyContacts() {
            for contact in emergencyContacts where contact.contactId != nil {
                self.emergencyContacts[contact.contactId!] = contact
            }
        }
        
        self.view.addSubview(self.searchBar)
        self.view.addSubview(self.tableView)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
    
        if !self.view.subviews.contains(self.letterViewController.view) {
            let width:CGFloat = 25.0
            let height:CGFloat = self.view.bounds.size.height - self.searchBar.frame.maxY - 40.0
            self.letterViewController.view.frame = CGRect(
                x: self.view.bounds.size.width - width,
                y: self.searchBar.frame.maxY,
                width: width,
                height: height
            )
            
            self.addChildViewController(self.letterViewController)
            self.view.addSubview(self.letterViewController.view)
            self.view.bringSubview(toFront: self.letterViewController.view)
            self.letterViewController.didMove(toParentViewController: self)
        }
        
        NotificationCenter.default.addObserver(
            forName: NSNotification.Name.UIKeyboardWillShow,
            object: nil,
            queue: nil,
            using: keyboardWillShow
        )
        
        NotificationCenter.default.addObserver(
            forName: NSNotification.Name.UIKeyboardWillHide,
            object: nil,
            queue: nil,
            using: keyboardWillDisappear
        )
    }
    
    func getContacts() {
        DispatchQueue.global().async {
            for letter in self.letters {
                self.contacts[letter] = [CNContact]()
            }
            
            do {
                try self.contactHandler.allContacts(completion: { (fetchedContacts:[CNContact]) in
                    DispatchQueue.main.async {
                        for contact in fetchedContacts {
                            if contact.givenName.isEmpty {
                                continue
                            }
                            
                            let index = contact.givenName.index(contact.givenName.startIndex, offsetBy: 1)
                            let firstLetter = contact.givenName.substring(to: index).lowercased()
                            if contact.givenName.characters.count > 0 && firstLetter != " "{
                                if var contacts:[CNContact] = self.contacts[firstLetter] {
                                    contacts.append(contact)
                                    self.contacts[firstLetter] = contacts
                                }
                            }
                        }
                        
                        for (letter, unsortedContacts) in self.contacts {
                            let contacts = unsortedContacts.sorted(by: { (contact1, contact2) -> Bool in
                                if contact1.givenName.lowercased() == contact2.givenName.lowercased() {
                                    return contact1.familyName.lowercased() < contact2.familyName.lowercased()
                                }
                                
                                return contact1.givenName.lowercased() < contact2.givenName.lowercased()
                            })
                            
                            self.contacts[letter] = contacts
                        }
                        
                        self.displayedContacts = self.contacts
                        
                        self.tableView.reloadData()
                    }
                })
            } catch {
                print("error retreiving contacts")
            }
        }
    }
    
    func getContactsFromCurrentText() {
        if self.currentText == "" {
            self.displayedContacts = self.contacts
            return
        }
        
        for (letter, contacts) in self.contacts {
            var sortedContacts = [CNContact]()
            var i = contacts.count - 1
            while i >= 0 {
                let contact = contacts[i]
                let name = self.contactHandler.fullNameForContact(contact: contact).lowercased()
                if name.range(of: self.currentText.lowercased()) != nil {
                    sortedContacts.append(contact)
                }
                
                i -= 1
            }
            
            if sortedContacts.count == 0 {
                self.displayedContacts.removeValue(forKey: letter)
            } else {
                self.displayedContacts[letter] = sortedContacts.reversed()
            }
        }
    }
    
    func keyForSection(section: Int) -> String? {
        var counter = 0
        for letter in self.letters {
            if self.displayedContacts[letter] != nil {
                if counter == section {
                    return letter
                }
                
                counter += 1
            }
        }
        
        return nil
    }
    
    func sectionForKey(key: String) -> Int? {
        if self.displayedContacts[key] == nil {
            return nil
        }
        
        let keys = self.displayedContacts.keys.sorted()
        return keys.index(of: key)
    }
    
    func keyboardWillShow(notification: Notification) {
        if let info = notification.userInfo, let frameValue = info[UIKeyboardFrameEndUserInfoKey] as? NSValue {
            let keyboardFrame: CGRect = frameValue.cgRectValue
            let appDelegate:SLAppDelegate = UIApplication.shared.delegate as! SLAppDelegate
            let rootVC: UIViewController = appDelegate.window.rootViewController!
            let convertedFrame = self.view.convert(keyboardFrame, from: rootVC.view)
            
            self.tableYOffset = keyboardFrame.height - (keyboardFrame.origin.y - convertedFrame.origin.y)
            
            if let navController = self.navigationController {
                self.tableYOffset += navController.navigationBar.bounds.size.height
                    + UIApplication.shared.statusBarFrame.size.height
            }
            
            self.tableView.contentInset = UIEdgeInsetsMake(
                self.tableView.contentInset.top,
                self.tableView.contentInset.left,
                self.tableView.contentInset.bottom + self.tableYOffset,
                self.tableView.contentInset.right
            )
        }
    }
    
    func keyboardWillDisappear(notification: Notification) {
        if let info = notification.userInfo,
            let durration:Double = info[UIKeyboardAnimationDurationUserInfoKey] as? Double,
            let curve = info[UIKeyboardAnimationCurveUserInfoKey] as? NSNumber {
            
            UIView.animate(
                withDuration: durration,
                delay: 0,
                options: UIViewAnimationOptions(rawValue: curve.uintValue),
                animations: {
                    self.tableView.contentInset = UIEdgeInsetsMake(
                        self.tableView.contentInset.top,
                        self.tableView.contentInset.left,
                        self.tableView.contentInset.bottom - self.tableYOffset,
                        self.tableView.contentInset.right
                    )
                },
                completion: nil
            )
        }
    }

    // MARK: UITableViewDelegate & DataSource Methods
    func numberOfSections(in tableView: UITableView) -> Int {
        return self.displayedContacts.keys.count
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        guard let key = self.keyForSection(section: section) else {
            return 0
        }
        
        if let contacts = self.displayedContacts[key] {
            return contacts.count
        }
        
        return 0
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var text = ""
        var isSelectedContact = false
        if let key = self.keyForSection(section: indexPath.section) {
            let contact:CNContact = self.displayedContacts[key]![indexPath.row]
            text = self.contactHandler.fullNameForContact(contact: contact)
            isSelectedContact = self.emergencyContacts[contact.identifier] != nil
        }
        
        var cell: SLAddContactTableViewCell? = tableView.dequeueReusableCell(
            withIdentifier: self.contactCellId,
            for: indexPath as IndexPath
            ) as? SLAddContactTableViewCell
        
        if cell == nil {
            cell = SLAddContactTableViewCell(style: .default, reuseIdentifier: self.contactCellId)
        }
        
        cell!.selectionStyle = .none
        cell!.textLabel!.text = text
        cell!.isSelectedContact = isSelectedContact
        
        return cell!
    }
    
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return section == 0 ? 80.0 : 26.0
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        var view:UIView
        let labelWrapperView:UIView
        let letterHeight:CGFloat = 14.0
        let xSpacer:CGFloat = 10.0
        
        if section == 0 {
            let frame = CGRect(
                x: 0.0,
                y: 0.0,
                width: self.tableView.bounds.size.width,
                height: self.tableView(tableView, heightForHeaderInSection: section)
            )
            
            view = UIView(frame: frame)
            view.backgroundColor = UIColor.white
            
            let height:CGFloat = 16.0
            let labelFrame = CGRect(
                x: 0.0,
                y: 0.5*(view.bounds.size.height - height) - 0.5*self.tableView(
                    tableView,
                    heightForHeaderInSection: 1
                ),
                width: view.bounds.size.width,
                height: height
            )
            
            let label:UILabel = UILabel(frame: labelFrame)
            label.font = UIFont.systemFont(ofSize: 14)
            label.textColor = UIColor(white: 155.0/255.0, alpha: 1.0)
            label.text = NSLocalizedString("Nominate up to 3 emergency contacts.", comment: "")
            label.textAlignment = .center
            
            view.addSubview(label)

            let labelWrapperViewFrame = CGRect(
                x: 0.0,
                y: view.bounds.size.height - self.tableView(tableView, heightForHeaderInSection: 1),
                width: self.tableView.bounds.size.width,
                height: self.tableView(tableView, heightForHeaderInSection: 1)
            )
            labelWrapperView = UIView(frame: labelWrapperViewFrame)
            labelWrapperView.backgroundColor = UIColor(white: 242.0/255.0, alpha: 1.0)
            
            view.addSubview(labelWrapperView)
            
            let letterLabelFrame = CGRect(
                x: xSpacer,
                y: 0.5*(labelWrapperView.bounds.size.height - letterHeight),
                width: view.bounds.size.width - xSpacer,
                height: letterHeight
            )
            
            var text = ""
            if let letter = self.keyForSection(section: section) {
                text = letter.uppercased()
            }
            let letterLabel:UILabel = UILabel(frame: letterLabelFrame)
            letterLabel.font = UIFont.systemFont(ofSize: 12.0)
            letterLabel.textColor = UIColor(white: 155.0/255.0, alpha: 1.0)
            letterLabel.text = text
            
            labelWrapperView.addSubview(letterLabel)
        } else {
            let frame = CGRect(
                x: 0.0,
                y: 0.0,
                width: self.tableView.bounds.size.width,
                height: self.tableView(tableView, heightForHeaderInSection: section)
            )
            view = UIView(frame: frame)
            view.backgroundColor = UIColor(white: 242.0/255.0, alpha: 1.0)
            
            let labelWrapperViewFrame = CGRect(
                x: 0.0,
                y: view.bounds.size.height - self.tableView(tableView, heightForHeaderInSection: 1),
                width: self.tableView.bounds.size.width,
                height: self.tableView(tableView, heightForHeaderInSection: 1)
            )
            
            labelWrapperView = UIView(frame: labelWrapperViewFrame)
            labelWrapperView.backgroundColor = UIColor(white: 242.0/255.0, alpha: 1.0)

            view.addSubview(labelWrapperView)
            
            let letterLabelFrame = CGRect(
                x: xSpacer,
                y: 0.5*(labelWrapperView.bounds.size.height - letterHeight),
                width: view.bounds.size.width - xSpacer,
                height: letterHeight
            )
            
            var text = ""
            if let letter = self.keyForSection(section: section) {
                text = letter.uppercased()
            }
            let letterLabel:UILabel = UILabel(frame: letterLabelFrame)
            letterLabel.font = UIFont.systemFont(ofSize: 12.0)
            letterLabel.textColor = UIColor(white: 155.0/255.0, alpha: 1.0)
            letterLabel.text = text
            
            labelWrapperView.addSubview(letterLabel)
        }
    
        return view
    }
    
    func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        return 0.000001
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        guard let key = self.keyForSection(section: indexPath.section) else {
            return
        }
        let cell = self.tableView(tableView, cellForRowAt: indexPath) as! SLAddContactTableViewCell
        cell.isSelectedContact = !cell.isSelectedContact
        cell.setNeedsDisplay()
        
        let contact = self.displayedContacts[key]![indexPath.row]
        let emergencyContact = self.contactHandler.emergencyContactFromCNContact(contact: contact)
        self.delegate?.contactViewControllerContactSelected(
            cvc: self,
            contact: emergencyContact,
            isSelected: cell.isSelectedContact
        )
    }
    
    // MARK: SLContactsLetterViewControllerDelegate methods
    func contactsLetterViewController(letterViewController: SLContactsLetterViewController, letter: String) {
        let key = letter.lowercased()
        guard let section = self.sectionForKey(key: key) else {
            return
        }
        
        guard let contacts = self.displayedContacts[key] else {
            return
        }
        
        if contacts.count == 0 {
            return
        }
        
        let indexPath = IndexPath(row: 0, section: section)
        self.tableView.scrollToRow(at: indexPath, at: .top, animated: true)
    }
    
    // MARK: SLSearchBarDelegate methods
    func searchBarTextDidBeginEditing(_ searchBar: UISearchBar) {
        searchBar.showsCancelButton = true
    }

    func searchBarTextDidEndEditing(_ searchBar: UISearchBar) {
        searchBar.showsCancelButton = false
        searchBar.text = ""
        self.currentText = ""
        self.displayedContacts = self.contacts
        self.tableView.reloadData()
    }
    
    func searchBarCancelButtonClicked(_ searchBar: UISearchBar) {
        searchBar.resignFirstResponder()
    }
    
    func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
        self.currentText = searchText
        self.getContactsFromCurrentText()
        self.tableView.reloadData()
    }
}
