package com.twister.cineworld.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.*;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.twister.cineworld.model.json.*;
import com.twister.cineworld.ui.Tools;

public abstract class BaseListActivity<T, UI> extends Activity implements AsyncAccessor<T, UI> {
	private AbsListView	m_listView;
	private int			m_contentViewId;
	private int			m_contextMenuId;

	public BaseListActivity(final int contentViewId, final int contextMenuId) {
		m_contentViewId = contentViewId;
		m_contextMenuId = contextMenuId;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Tools.s_context = this;
		setContentView(m_contentViewId);

		m_listView = (AbsListView) findViewById(android.R.id.list);
		registerForContextMenu(m_listView);
	}

	@Override
	protected void onStart() {
		super.onStart();
		new AsyncAccessorExecutor(this).execute(this);
	}

	@Override
	public final void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(m_contextMenuId, menu);

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		assert v == m_listView;
		AbsListView list = (AbsListView) v;
		@SuppressWarnings("unchecked")
		UI adapterItem = (UI) list.getAdapter().getItem((int) info.id);
		onCreateContextMenu(menu, adapterItem);
	}

	protected abstract void onCreateContextMenu(final ContextMenu menu, final UI item);

	@Override
	public final boolean onContextItemSelected(final MenuItem menu) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menu.getMenuInfo();
		@SuppressWarnings("unchecked")
		UI adapterItem = (UI) m_listView.getAdapter().getItem((int) info.id);
		return onContextItemSelected(menu, adapterItem);
	}

	protected abstract boolean onContextItemSelected(MenuItem menu, UI item);

	public AbsListView getListView() {
		return m_listView;
	}
}