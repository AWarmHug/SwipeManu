package com.warm.sideslip;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecy;

    private Adapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecy = (RecyclerView) findViewById(R.id.recy);
        mAdapter = new Adapter();
        mAdapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onClick(int position, int type) {

                switch (type) {
                    case Adapter.CLICK:
                        Toast.makeText(MainActivity.this, "点击了Item，位置：" + position, Toast.LENGTH_SHORT).show();
                        break;
                    case Adapter.ADD:
                        Toast.makeText(MainActivity.this, "点击了Add，位置：" + position, Toast.LENGTH_SHORT).show();
                        break;
                    case Adapter.DELETE:
                        Toast.makeText(MainActivity.this, "点击了Delete，位置：" + position, Toast.LENGTH_SHORT).show();

                        break;
                }
            }
        });
        mRecy.setAdapter(mAdapter);
        mRecy.setLayoutManager(new LinearLayoutManager(this));
    }

    static class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        public static final int CLICK = 0;
        public static final int ADD = 1;
        public static final int DELETE = 2;
        private Adapter.OnItemClickListener onItemClickListener;

        public void setOnItemClickListener(Adapter.OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false));
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            if (onItemClickListener != null) {
                holder.item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClickListener.onClick(holder.getAdapterPosition(), CLICK);
                    }
                });
                holder.add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClickListener.onClick(holder.getAdapterPosition(), ADD);
                    }
                });
                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClickListener.onClick(holder.getAdapterPosition(), DELETE);
                    }
                });
            }

        }

        @Override
        public int getItemCount() {
            return 30;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private LinearLayout item;
            private TextView top;
            private TextView add;
            private TextView delete;

            public ViewHolder(View itemView) {
                super(itemView);
                item = itemView.findViewById(R.id.tv_item);
                top = itemView.findViewById(R.id.tv_top);
                add = itemView.findViewById(R.id.tv_add);
                delete = itemView.findViewById(R.id.tv_delete);

            }
        }


        interface OnItemClickListener {
            void onClick(int position, int type);
        }

    }

}
