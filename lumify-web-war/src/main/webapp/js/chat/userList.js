define([
    'require',
    'flight/lib/component',
    'hbs!./userListTpl',
    'util/formatters'
], function(
    require,
    defineComponent,
    template,
    F) {
    'use strict';

    return defineComponent(UsersList);

    function UsersList() {

        this.defaultAttrs({
            userListItemSelector: '.users-list .user'
        });

        this.after('initialize', function() {
            this.$node.html(template({}));

            this.usersDeferred = $.Deferred();
            this.on('usersForChat', this.onUsersForChat);
            this.on(document, 'socketMessage', this.onSocketMessage);
            this.on(document, 'chatMessage', this.onChatMessage);
            this.on(document, 'selectUser', this.onSelectUser);
            this.on('click', {
                userListItemSelector: this.onUserListItemClicked
            });
            this.trigger('requestUsersForChat');
        });

        this.onChatMessage = function(event, data) {
            var self = this;
            this.usersDeferred.done(function() {
                var userRow = self.$node.find('.' + F.className.to(data.from.id) + '');

                if (!userRow.hasClass('active')) {
                    self.trigger('selectUser', { userId: data.from.id });
                }
            });
        };

        this.onUserListItemClicked = function(event) {
            event.preventDefault();

            var $target = $(event.target).closest('li');

            if ($target.length) {
                var userId = $target.data('userId');

                $target.find('.badge').text('');

                this.trigger('selectUser', { userId: userId });
            }
        };

        this.onSelectUser = function(event, data) {
            this.$node.find('.active').removeClass('active');
            if (data && data.userId) {
                this.$node.find('.' + F.className.to(data.userId)).addClass('active');
                this.trigger('userSelected', _.findWhere(this.users, { id: data.userId }));
            } else {
                this.trigger('userSelected');
            }
        };

        this.onSocketMessage = function(event, message) {
            if (message &&
                ~'userStatusChange userWorkspaceChange'.indexOf(message.type)) {
                var user = message.data;

                if (user.workspaceId) {
                    user.currentWorkspaceId = user.workspaceId;
                }

                if (this.users) {
                    var newUsers = _.reject(this.users, function(u) {
                        return user.id === u.id;
                    })
                    newUsers.push(user);
                    this.users = newUsers;
                    this.updateUsers();
                }
            }
        };

        this.onUsersForChat = function(event, data) {
            this.currentWorkspace = data.workspace;
            this.users = data.users;
            this.workspaces = _.indexBy(data.workspaces, 'workspaceId');
            this.updateUsers();
            this.usersDeferred.resolve();
        };

        this.updateUsers = function() {
            var self = this,
                UNKNOWN = 'Unknown',
                groupedUsers = _.chain(self.users)
                    .reject(function(user) {
                        return (/OFFLINE/i.test(user.status)) || user.id === window.currentUser.id;
                    })
                    .groupBy(function(user) {
                        user.cls = F.className.to(user.id);

                        if (!self.workspaces[user.currentWorkspaceId]) {
                            return UNKNOWN;
                        }

                        return user.currentWorkspaceId;
                    })
                    .value(),
                usersByWorkspace = _.chain(groupedUsers)
                    .keys()
                    .sortBy(function(id) {
                        return id === UNKNOWN ? '2' :
                            id === self.currentWorkspace.id ? '0' :
                            ('1' + self.workspaces[id].title.toLowerCase());
                    })
                    .map(function(id) {
                        return {
                            name: id === UNKNOWN ? UNKNOWN : self.workspaces[id].title,
                            users: groupedUsers[id]
                        };
                    })
                    .value(),
                activeUser = this.$node.find('.user.active').data('userId');

            this.$node.html(template({
                workspace: self.currentWorkspace,
                usersByWorkspace: usersByWorkspace
            }));
            if (activeUser) {
                var row = this.$node.find('.' + F.className.to(activeUser));

                if (row.length) {
                    row.addClass('active');
                } else {
                    this.trigger('selectUser');
                }
            }
        }
    }
});
